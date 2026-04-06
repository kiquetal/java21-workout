# Pessimistic Locking — Why You Lock What You Don't Write

## The Analogy: The Fitting Room

A clothing store has a rule: **you can only enter the fitting room if you have no unreturned items from last time**.

Without a lock:

```
Customer #42 walks up to fitting room A → clerk checks: "no unreturned items" ✅ → enters
Customer #42 walks up to fitting room B → clerk checks: "no unreturned items" ✅ → enters

Two clerks checked at the same time. Neither saw the other's decision.
Customer #42 is now in two fitting rooms — rule broken.
```

With a lock:

```
Customer #42 walks up → clerk A grabs customer #42's loyalty card 🔒
Customer #42 walks up again → clerk B asks for the card → "card is with clerk A" → WAIT ⏳

Clerk A checks: "no unreturned items" ✅ → lets them in → returns the card 🔓
Clerk B now gets the card → checks: "already in a fitting room" ❌ → REJECTED
```

The lock isn't on the fitting room (the table you're writing to). It's on the **customer's loyalty card** (the entity the rule revolves around). That's the coordination point.

## The Problem in Our Code

The lending flow:

1. Check if member has overdue books → `lendingRepository.hasOverdueBook(memberId)`
2. Check lending limit → `lendingRepository.listBookLendingBorrowed(memberId)`
3. Create the lending → `lendingRepository.persist(lending)`

Step 1 is a read on `book_lending`. Step 3 is a write on `book_lending`. But between step 1 and step 3, another thread can run the same check for the same member and also pass — because neither has committed yet.

```
Thread A                                Thread B
────────                                ────────
hasOverdueBook(42) → false              
                                        hasOverdueBook(42) → false
persist lending for member 42           
                                        persist lending for member 42
commit ✅                                commit ✅  ← both passed, rule broken
```

## The Fix: Lock the Member Row

The business rule is about the **member**. Two threads are racing to lend for the **same member**. So we lock the **member row** — even though we're reading/writing the `book_lending` table.

```java
// MemberRepository
public Optional<Member> findByMemberIdForUpdate(MemberId memberId) {
    return find("id", memberId.value())
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResultOptional();
}
```

The lending service locks the member first, then runs all checks:

```java
// LendingService
@Transactional
public LendingResult lend(LendCommand command) {
    // Step 1: lock member row — other threads for same member WAIT here
    var member = memberRepository.findByMemberIdForUpdate(command.memberId())
        .orElse(null);
    if (member == null) return new LendingResult.MemberNotFound(command.memberId());

    // Step 2: safe — no other thread can proceed for this member
    if (lendingRepository.hasOverdueBook(command.memberId())) {
        return new LendingResult.MemberHasOverdueBooks(...);
    }

    // Step 3: persist
    var lending = new BookLending();
    lending.member = member;
    // ...
    lendingRepository.persist(lending);
    return new LendingResult.Success(...);
}
```

Now the timeline becomes:

```
Thread A                                Thread B
────────                                ────────
lock member #42 🔒                       
                                        lock member #42 → WAIT ⏳
hasOverdueBook(42) → false              
persist lending                         
commit, release lock 🔓                 
                                        lock acquired 🔒
                                        hasOverdueBook(42) → true (sees A's data)
                                        REJECTED ❌
```

## Why Not Lock the Lending Table?

- **The rows don't exist yet** — you can't lock a row you're about to insert
- **Locking all lending rows** is too broad — blocks every member, not just #42
- **The member row** is the natural contention point — one row per member, surgical lock

## Key Takeaway

The lock goes on the **entity the business rule revolves around**, not the table you're reading or writing. The member row acts as a gate — it serializes all lending operations for that member, making the check-then-insert sequence atomic.
