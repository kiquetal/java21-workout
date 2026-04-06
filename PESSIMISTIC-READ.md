# Pessimistic Read — Freezing Rows You Won't Modify

## Quick Recap: The Three Levels

| Mode | SQL | Readers blocked? | Writers blocked? |
|---|---|---|---|
| No lock (default) | `SELECT` | No | No |
| `PESSIMISTIC_READ` | `SELECT ... FOR SHARE` | No | Yes |
| `PESSIMISTIC_WRITE` | `SELECT ... FOR UPDATE` | Yes | Yes |

`PESSIMISTIC_READ` = "I need this data stable, but I'm only reading, so let other readers in too."

## When to Use It

You read row A, then based on that data you write to row B (a different table), and you need row A to stay unchanged until you're done. You never modify row A itself.

This is rare. Most apps never need it. The real use cases are financial/regulatory where inconsistency has legal consequences.

## The Example: Daily Account Closing

A bank runs end-of-day closing. It reads the account balance, reads all transactions for the day, verifies they match, and writes a closing report. No transfers can be allowed to modify the account while this runs.

### Repository

```java
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    public Optional<Account> findForShare(Long ownerId) {
        return find("ownerId", ownerId)
                .withLock(LockModeType.PESSIMISTIC_READ)
                .firstResultOptional();
    }

    public Optional<Account> findForUpdate(Long ownerId) {
        return find("ownerId", ownerId)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional();
    }
}
```

### DailyClosingService — uses `PESSIMISTIC_READ`

Reads the account, reads transactions, writes to a different table (`ClosingReport`). Never modifies the account row.

```java
@ApplicationScoped
public class DailyClosingService {

    @Inject AccountRepository accountRepository;
    @Inject TransactionRepository transactionRepository;
    @Inject ClosingReportRepository closingReportRepository;

    @Transactional
    public ClosingResult close(Long accountId, LocalDate date) {
        // FOR SHARE — freezes the account row, but other closing threads can also read it
        var account = accountRepository.findForShare(accountId)
                .orElse(null);
        if (account == null) return new ClosingResult.NotFound(accountId);

        var transactions = transactionRepository.listByDate(accountId, date);

        var debits = transactions.stream()
                .filter(t -> t.type == TransactionType.DEBIT)
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var credits = transactions.stream()
                .filter(t -> t.type == TransactionType.CREDIT)
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var computed = account.balance.subtract(debits).add(credits);

        if (computed.compareTo(account.balance) != 0) {
            return new ClosingResult.Mismatch(accountId, account.balance, computed);
        }

        // Write goes to a DIFFERENT table — not the account row
        var report = new ClosingReport();
        report.accountId = accountId;
        report.date = date;
        report.balance = account.balance;
        report.totalDebits = debits;
        report.totalCredits = credits;
        closingReportRepository.persist(report);

        return new ClosingResult.Success(report);
    }
}
```

### TransferService — uses `PESSIMISTIC_WRITE`

Reads the account and modifies it. Needs exclusive access.

```java
@ApplicationScoped
public class TransferService {

    @Inject AccountRepository accountRepository;
    @Inject TransactionRepository transactionRepository;

    @Transactional
    public TransferResult transfer(Long senderId, Long receiverId, BigDecimal amount) {
        // FOR UPDATE — exclusive lock, must wait if anyone holds FOR SHARE
        var sender = accountRepository.findForUpdate(senderId).orElse(null);
        if (sender == null) return new TransferResult.SenderNotFound(senderId);

        if (sender.balance.compareTo(amount) < 0) {
            return new TransferResult.InsufficientFunds(senderId);
        }

        var receiver = accountRepository.findForUpdate(receiverId).orElse(null);
        if (receiver == null) return new TransferResult.ReceiverNotFound(receiverId);

        sender.balance = sender.balance.subtract(amount);
        receiver.balance = receiver.balance.add(amount);

        var tx = new Transaction();
        tx.accountId = senderId;
        tx.amount = amount.negate();
        tx.type = TransactionType.DEBIT;
        transactionRepository.persist(tx);

        return new TransferResult.Success(senderId, receiverId, amount);
    }
}
```

### How They Interact

```
DailyClosingService (Thread A): FOR SHARE on account 1 ✅ (shared lock)
DailyClosingService (Thread B): FOR SHARE on account 1 ✅ (shared lock — no conflict)
TransferService     (Thread C): FOR UPDATE on account 1 ⏳ BLOCKED — waits for A and B

Thread A: reads transactions, builds report, commits → releases lock
Thread B: reads transactions, builds report, commits → releases lock

Thread C: FOR UPDATE on account 1 ✅ — now gets exclusive lock
         → updates balance, inserts transaction, commits → releases lock
```

Threads A and B ran in parallel because `FOR SHARE` doesn't block other `FOR SHARE`. If they had used `FOR UPDATE`, B would have waited for A for no reason — neither is writing the account row.

## Why Not Just No Lock?

Without `FOR SHARE`, a transfer could modify the account balance and insert a transaction between the two reads in the closing service. The report would show a balance that doesn't match the transactions — a regulatory violation.

## When You Don't Need It

- Book lending apps, shopping carts, blogs, task managers — almost never
- If slightly stale reads are acceptable — no lock needed
- If you're going to modify the row — use `PESSIMISTIC_WRITE` instead

`PESSIMISTIC_READ` solves problems that most applications don't have. If you're struggling to find a use case for your domain, that's the right conclusion.
