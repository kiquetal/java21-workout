# Locking Exercises

Test your understanding. For each scenario, answer:
1. Is there a race condition?
2. If yes, what's the consequence?
3. What would you lock, and why?

Answers are at the bottom — don't peek.

---

## Scenario 1: The Double Reservation

Business rule: **A member can only have one active reservation per book.**

```java
@Transactional
public ReservationResult reserve(MemberId memberId, BookId bookId) {
    var existing = reservationRepository.findActiveByMemberAndBook(memberId, bookId);
    if (existing.isPresent()) {
        return new ReservationResult.AlreadyReserved();
    }

    var reservation = new Reservation();
    reservation.member = memberRepository.findByMemberId(memberId).orElseThrow();
    reservation.book = bookRepository.findById(bookId.value());
    reservation.status = ReservationStatus.ACTIVE;
    reservationRepository.persist(reservation);
    return new ReservationResult.Success(reservation);
}
```

Two requests arrive at the same time: member #42 reserves book #7 twice.

---

## Scenario 2: The Wallet Transfer

Business rule: **A wallet balance can never go negative.**

```java
@Transactional
public TransferResult transfer(WalletId from, WalletId to, BigDecimal amount) {
    var sender = walletRepository.findById(from);
    if (sender.balance.compareTo(amount) < 0) {
        return new TransferResult.InsufficientFunds();
    }

    var receiver = walletRepository.findById(to);
    sender.balance = sender.balance.subtract(amount);
    receiver.balance = receiver.balance.add(amount);
    return new TransferResult.Success();
}
```

Wallet #1 has $100. Two threads transfer $80 from wallet #1 at the same time.

---

## Scenario 3: The Event Seat Limit

Business rule: **An event has a maximum of 100 attendees.**

```java
@Transactional
public RegistrationResult register(EventId eventId, MemberId memberId) {
    var event = eventRepository.findByIdForUpdate(eventId);  // 🔒 locks event row
    var count = registrationRepository.countByEvent(eventId);
    if (count >= 100) {
        return new RegistrationResult.EventFull();
    }

    var registration = new Registration();
    registration.event = event;
    registration.member = memberRepository.findByMemberId(memberId).orElseThrow();
    registrationRepository.persist(registration);
    return new RegistrationResult.Success();
}
```

Is this correct? What's being locked and why?

---

## Scenario 4: The Coupon Code

Business rule: **A coupon can only be redeemed once across all users.**

```java
@Transactional
public RedeemResult redeem(CouponCode code, MemberId memberId) {
    var coupon = couponRepository.findByCode(code);  // plain read, no lock
    if (coupon == null) return new RedeemResult.NotFound();
    if (coupon.redeemed) return new RedeemResult.AlreadyRedeemed();

    coupon.redeemed = true;
    coupon.redeemedBy = memberId;
    return new RedeemResult.Success();
}
```

Two users try to redeem the same coupon at the same time.

---

## Scenario 5: The Lending Service (Your Code)

A colleague writes a new method that bypasses the lock:

```java
@Transactional
public LendingResult quickLend(LendCommand command) {
    // "we don't need the lock, we're just checking and inserting"
    var member = memberRepository.findByMemberId(command.memberId())
        .orElse(null);
    if (member == null) return new LendingResult.MemberNotFound(command.memberId());

    if (lendingRepository.hasOverdueBook(command.memberId())) {
        return new LendingResult.MemberHasOverdueBooks(...);
    }

    var lending = new BookLending();
    lending.member = member;
    // ...
    lendingRepository.persist(lending);
    return new LendingResult.Success(...);
}
```

Meanwhile your `lend()` method properly locks the member row. Both methods are exposed via different endpoints. What happens?

---
---
---

# Answers

## Scenario 1: The Double Reservation

**Race condition: YES.**

```
Thread A: findActiveByMemberAndBook(42, 7) → empty
Thread B: findActiveByMemberAndBook(42, 7) → empty
Thread A: persist reservation ✅
Thread B: persist reservation ✅  ← duplicate
```

**What to lock:** The member row. Both threads are reserving for the same member — lock the member before checking. Same pattern as the lending service.

Alternative: a unique partial index on `(member_id, book_id) WHERE status = 'ACTIVE'` as a safety net at the DB level.

---

## Scenario 2: The Wallet Transfer

**Race condition: YES.**

```
Thread A: sender.balance = $100, $100 >= $80 ✅
Thread B: sender.balance = $100, $100 >= $80 ✅
Thread A: balance = $100 - $80 = $20
Thread B: balance = $100 - $80 = $20  ← should be -$60, but both read $100
```

Final balance: $20 instead of -$60. The sender "created" $60 out of thin air.

**What to lock:** The sender wallet row. Use `findByIdForUpdate(from)` — this is one of the rare cases where you lock the row you're actually writing to, because the wallet row IS the contention point.

Note: you do NOT need to lock the receiver — two threads adding to the same receiver don't conflict in a dangerous way (worst case: one overwrites the other's add, which you'd catch with `@Version` optimistic locking).

---

## Scenario 3: The Event Seat Limit

**This one is CORRECT. No race condition.**

The event row is locked with `findByIdForUpdate`. All registration attempts for the same event must acquire this lock first. Thread B waits until Thread A commits, then sees the updated count.

The lock is on the **event row** — because the business rule is about the event ("max 100 attendees for *this event*"). Same logic: lock the entity the rule revolves around.

---

## Scenario 4: The Coupon Code

**Race condition: YES.**

```
Thread A: coupon.redeemed = false ✅
Thread B: coupon.redeemed = false ✅
Thread A: coupon.redeemed = true
Thread B: coupon.redeemed = true  ← redeemed twice
```

**What to lock:** The coupon row itself. `findByCodeForUpdate(code)` — because the coupon IS the contention point. Two users are racing to claim the same coupon.

This is different from the lending scenario. There, you couldn't lock lending rows because they didn't exist yet. Here, the coupon row already exists and is exactly what both threads are fighting over.

---

## Scenario 5: The Lending Service (Your Code)

**The lock is completely broken.**

Your `lend()` locks the member row. But `quickLend()` uses `findByMemberId()` — a plain read, no lock. The database doesn't block `quickLend()` because it never asks for a lock.

```
Thread A (lend):      lock member #42 🔒
Thread B (quickLend): findByMemberId(42) → goes right through, no lock requested
Thread B: hasOverdueBook → false
Thread B: persist lending ✅  ← bypassed the lock entirely
```

**The lesson:** The lock is a convention enforced by your code. If ANY code path that modifies lending data skips the lock, the whole strategy falls apart. Every method that checks-then-inserts for a member must go through the same locked entry point.
