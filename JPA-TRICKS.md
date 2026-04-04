# JPA Relationship Tricks

## Ownership = Who Has the Foreign Key

The **owning side** is the table that has the FK column. Always the `@ManyToOne` side.

```
book_item table
├── id
└── book_id ← FK lives here, so BookItem OWNS the relationship
```

```java
@Entity
public class BookItem extends PanacheEntity {
    @ManyToOne(optional = false)   // I own the relationship
    public Book book;              // this creates the book_id FK column
}
```

`Book` has no FK pointing to `BookItem`. So `Book` is NOT the owner — even if you add `@OneToMany` on it.

## @JoinColumn — Customize the FK Column

By default, Hibernate names the FK column `<field>_id` (e.g., `book_id`). Use `@JoinColumn` to override:

```java
@ManyToOne
@JoinColumn(name = "fk_book")   // column will be "fk_book" instead of "book_id"
public Book book;
```

`@JoinColumn` goes on the **owning side** (the one with `@ManyToOne`).

## @OneToMany(mappedBy) — The Mirror (Non-Owning Side)

```java
@Entity
public class Book extends PanacheEntity {
    @OneToMany(mappedBy = "book")   // "book" = field name in BookItem
    public Set<BookItem> items;     // NO new column or table — just a mirror
}
```

`mappedBy` means: "I don't own this. The FK is on BookItem.book. I'm just reading it."

**Without `mappedBy`** → Hibernate thinks BOTH sides own it → creates a join table. Bad.

## The Rules

```
@ManyToOne                          → always the owner, always has the FK
@OneToMany(mappedBy = "fieldName")  → never the owner, just a mirror
@OneToMany (no mappedBy)            → creates a join table — avoid this
@JoinColumn                         → put on the owning side to customize FK name
```

## Unidirectional vs Bidirectional

**Unidirectional** — only the child knows the parent:

```java
// BookItem.java
@ManyToOne
public Book book;     // BookItem → Book ✅

// Book.java
// nothing about BookItem — Book doesn't know
```

**Bidirectional** — both sides know each other:

```java
// BookItem.java
@ManyToOne
public Book book;     // BookItem → Book ✅

// Book.java
@OneToMany(mappedBy = "book")
public Set<BookItem> items;   // Book → BookItems ✅
```

Start unidirectional. Add the other side only when you need it.

## Cascade and Orphan Removal

```java
@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
public Set<BookItem> items;
```

| Option | What it does |
|---|---|
| `cascade = ALL` | Persist/delete Book → also persists/deletes its BookItems |
| `orphanRemoval = true` | Remove a BookItem from the set → deletes it from DB |

Use with care — deleting a `Book` would delete all its `BookItem`s. Only use when the child can't exist without the parent.

## Fetch Types

```java
@ManyToOne(fetch = FetchType.LAZY)    // don't load Book when loading BookItem
@ManyToOne(fetch = FetchType.EAGER)   // always load Book with BookItem (default for @ManyToOne)

@OneToMany(fetch = FetchType.LAZY)    // don't load items when loading Book (default for @OneToMany)
@OneToMany(fetch = FetchType.EAGER)   // always load items with Book — avoid this
```

Defaults:

| Annotation | Default fetch |
|---|---|
| `@ManyToOne` | EAGER |
| `@OneToMany` | LAZY |

Best practice: make `@ManyToOne` lazy too:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
public Book book;
```

## Why Avoid @ManyToMany

```java
// ❌ Hidden join table, can't add columns
@ManyToMany
public Set<Author> authors;
```

Creates:

```sql
book_author (book_id, author_id)   -- no room for "role", "ordering", etc.
```

Instead, make the join table an explicit entity:

```java
// ✅ You control the table
@Entity
public class BookAuthor extends PanacheEntity {
    @ManyToOne public Book book;
    @ManyToOne public Author author;
    public String role;       // "primary", "editor"
}
```

Two `@ManyToOne` = you own the join table. Can add any columns you need.

## Use Set, Not List for Collections

```java
// ❌ List — Hibernate deletes all + re-inserts on any change
@OneToMany(mappedBy = "book")
public List<BookItem> items;

// ✅ Set — Hibernate deletes/inserts only what changed
@OneToMany(mappedBy = "book")
public Set<BookItem> items;
```

## N+1 Problem

Loading 10 books, each with items:

```java
var books = bookRepository.listAll();  // 1 query: SELECT * FROM book
for (var book : books) {
    book.items.size();                 // 10 queries: SELECT * FROM book_item WHERE book_id = ?
}
// Total: 11 queries for 10 books ❌
```

Fix with `@Fetch(FetchMode.SUBSELECT)`:

```java
@OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
@Fetch(FetchMode.SUBSELECT)
public Set<BookItem> items;
// 1 query for books + 1 query for ALL items = 2 queries ✅
```

Or fix with a JPQL join fetch:

```java
// In repository
public List<Book> findAllWithItems() {
    return find("SELECT b FROM Book b LEFT JOIN FETCH b.items").list();
}
// 1 query ✅
```

## @OneToOne — Tricky, Prefer @ManyToOne

```java
// ❌ @OneToOne on the non-owning side is ALWAYS eager loaded
@Entity
public class Member extends PanacheEntity {
    @OneToOne(mappedBy = "member")
    public MemberProfile profile;   // Hibernate can't proxy this — always loads it
}

// ✅ Use @ManyToOne instead — lazy works correctly
@Entity
public class MemberProfile extends PanacheEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    public Member member;   // just don't add the other side
}
```

`@OneToOne` on the non-owning side can't be lazy because Hibernate needs to know if it's null or not — which requires a query. `@ManyToOne` doesn't have this problem.

## @Enumerated — Always Use STRING

```java
// ❌ Default is ORDINAL — stores 0, 1, 2 — breaks if you reorder the enum
@Enumerated
public LendingStatus status;

// ✅ STRING — stores "ACTIVE", "RETURNED" — safe to reorder
@Enumerated(EnumType.STRING)
@Column(length = 20)
public LendingStatus status;
```

## @Column Tricks

```java
// Not nullable
@Column(nullable = false)
public String title;

// Fixed length (useful for ISBN, status codes)
@Column(length = 13)
public String isbn;

// Not updatable — set once, never change
@Column(updatable = false)
public LocalDate createdAt;

// Not insertable — DB generates it (e.g., trigger, default)
@Column(insertable = false)
public LocalDate generatedField;

// Unique constraint
@Column(unique = true)
public String email;
```

### What @Column Actually Does

`@Column` does NOT validate your Java code. It only affects DDL generation and documents the mapping:

```java
@Column(nullable = false)
public String title;
```

This tells Hibernate: "when generating the schema, add `NOT NULL` to this column." It does NOT prevent you from setting `title = null` in Java — that will fail at the DB level, not in your code.

| @Column property | Affects DDL? | Validates Java? | What it does |
|---|---|---|---|
| `nullable = false` | Yes → `NOT NULL` | No | DB rejects null |
| `unique = true` | Yes → `UNIQUE` constraint | No | DB rejects duplicates |
| `length = 13` | Yes → `VARCHAR(13)` | No | DB truncates/rejects |
| `updatable = false` | No DDL | Yes (Hibernate) | Hibernate skips column in UPDATE |
| `insertable = false` | No DDL | Yes (Hibernate) | Hibernate skips column in INSERT |

For Java-side validation, use Bean Validation (`@NotNull`, `@Size`) on your DTOs.

### @Column(unique = true) vs @Table(uniqueConstraints)

`@Column(unique = true)` creates a constraint with an auto-generated ugly name:

```java
@Column(unique = true)
public String isbn;
// → constraint name: ukbi5lx9jtv1f52idrmc0ck8ysx (random hash)
```

When Hibernate's `update` strategy runs on a fresh DB, it tries to drop this constraint first (in case it changed), then recreate it. On a new DB the drop fails → you see the warning:

```
constraint "ukbi5lx9jtv1f52idrmc0ck8ysx" of relation "book" does not exist, skipping
```

This is harmless — the constraint gets created correctly. The warning disappears on subsequent restarts because the constraint exists.

To get readable constraint names, use `@Table` instead:

```java
// ❌ Auto-generated name — hard to debug
@Entity
public class Book extends PanacheEntity {
    @Column(unique = true, nullable = false, length = 13)
    public String isbn;
}
// constraint name: ukbi5lx9jtv1f52idrmc0ck8ysx

// ✅ Named constraint — shows up clearly in error messages
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
    name = "uk_book_isbn",
    columnNames = "isbn"
))
public class Book extends PanacheEntity {
    @Column(nullable = false, length = 13)
    public String isbn;   // no unique=true here — @Table handles it
}
// constraint name: uk_book_isbn
```

Now when your `PersistenceExceptionMapper` catches a constraint violation, you see `uk_book_isbn` instead of random characters.

### Schema Management Strategies

```properties
# Dev — Hibernate adds tables/columns as you create entities, never drops
%dev.quarkus.hibernate-orm.schema-management.strategy=update

# Prod — Flyway owns the schema, Hibernate doesn't touch it
%prod.quarkus.hibernate-orm.schema-management.strategy=none
```

| Strategy | What it does | When to use |
|---|---|---|
| `none` | Hibernate doesn't touch the schema | Production (Flyway owns it) |
| `update` | Adds new tables/columns, never drops | Dev — incremental, keeps data |
| `drop-and-create` | Drops everything, recreates from entities | Dev — clean slate every restart |
| `validate` | Checks schema matches entities, fails if not | CI — catch mismatches |

With `update`, the workflow is:

```
1. Write entity → Hibernate creates table
2. Add field   → Hibernate adds column
3. Add @Column(unique=true) → Hibernate adds constraint
4. Restart     → Hibernate checks what exists, only adds what's missing
```

When ready for production:
```
5. Dev UI → Flyway → Create Initial Migration (exports the DDL)
6. Switch to strategy=none
7. Flyway owns the schema from now on
```

## @Table — Customize Table Name

```java
// Class name doesn't match table name
@Entity
@Table(name = "book_lending")
public class BookLending extends PanacheEntity { }
```

Without `@Table`, Hibernate uses the class name as the table name. Use it when your table name has underscores or differs from the class name.

## Composite Unique Constraints

```java
@Entity
@Table(
    name = "book_author",
    uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "author_id"})
)
public class BookAuthor extends PanacheEntity {
    @ManyToOne public Book book;
    @ManyToOne public Author author;
}
```

Prevents duplicate book-author pairs at the DB level.

## @PrePersist / @PreUpdate — Auto-Set Fields

```java
@Entity
public class Book extends PanacheEntity {
    public String title;

    @Column(updatable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

No need to set timestamps manually — Hibernate calls these before persist/update.

## Panache Projections — Query Into Records

Don't load the full entity when you only need a few fields:

```java
// DTO
public record BookSummary(String title, String author) {}

// Repository
public List<BookSummary> findSummaries() {
    return find("SELECT title, author FROM Book")
        .project(BookSummary.class)
        .list();
}
```

Only fetches `title` and `author` — no entity overhead, no lazy loading traps.

## Common Mistakes

| Mistake | Problem | Fix |
|---|---|---|
| `@OneToMany` without `mappedBy` | Creates hidden join table | Add `mappedBy = "fieldName"` |
| `@ManyToOne` default fetch (EAGER) | Always loads parent | Add `fetch = FetchType.LAZY` |
| `@Enumerated` without STRING | Stores ordinal, breaks on reorder | Use `@Enumerated(EnumType.STRING)` |
| `List` in collections | Inefficient deletes | Use `Set` |
| `@OneToOne` bidirectional | Non-owning side always eager | Use `@ManyToOne` or keep unidirectional |
| Missing `@Transactional` on update | Changes never flush | Add `@Transactional` |
| `entity.persist()` in service | Bypasses repository | Use `repository.persist(entity)` |

---

## Advanced Hacks

### Set vs List — The Real Reason

It's about how Hibernate identifies elements in the collection.

**List** — ordered, allows duplicates. Hibernate uses **position** to identify elements:

```
items[0] = BookItem#1
items[1] = BookItem#2
items[2] = BookItem#3
```

Remove `items[1]` → Hibernate doesn't know if the list shifted. It deletes ALL and re-inserts:

```sql
DELETE FROM book_item WHERE book_id = 1          -- delete all
INSERT INTO book_item (id, book_id) VALUES (1,1) -- re-insert remaining
INSERT INTO book_item (id, book_id) VALUES (3,1)
```

**Set** — unordered, no duplicates. Hibernate uses **entity identity** (PK) to identify elements:

Remove `BookItem#2` → Hibernate knows exactly which row:

```sql
DELETE FROM book_item WHERE id = 2   -- just this one
```

**The exception**: `List` with `@OrderColumn` behaves like an indexed collection and avoids the delete-all problem — but adds an ordering column to the table:

```java
@OneToMany(mappedBy = "book")
@OrderColumn(name = "position")   // adds "position" INT column to book_item
public List<BookItem> items;      // now List is efficient, but you need the column
```

### @Immutable — Read-Only Entities

For reference data that never changes (countries, categories, statuses):

```java
@Entity
@Immutable
public class Country extends PanacheEntity {
    public String code;
    public String name;
}
```

Hibernate will:
- Never generate UPDATE statements for this entity
- Never dirty-check it (faster)
- Throw an exception if you try to modify it

### @DynamicUpdate — Only Update Changed Columns

By default, Hibernate updates ALL columns even if you changed one:

```sql
-- Default: you changed only "name" but Hibernate sends everything
UPDATE member SET name='Bob', email='bob@test.com' WHERE id = 1
```

With `@DynamicUpdate`:

```java
@Entity
@DynamicUpdate
public class Member extends PanacheEntity {
    public String name;
    public String email;
}
```

```sql
-- Only the changed column
UPDATE member SET name='Bob' WHERE id = 1
```

Trade-off: Hibernate must diff every field on flush. Worth it for wide tables (many columns), not worth it for small entities.

### @DynamicInsert — Skip Null Columns on Insert

```java
@Entity
@DynamicInsert
public class BookLending extends PanacheEntity {
    public LocalDate borrowedAt;
    public LocalDate returnedAt;   // null on creation
    public LendingStatus status;
}
```

Without `@DynamicInsert`:
```sql
INSERT INTO book_lending (borrowed_at, returned_at, status) VALUES ('2026-04-03', NULL, 'ACTIVE')
```

With `@DynamicInsert`:
```sql
INSERT INTO book_lending (borrowed_at, status) VALUES ('2026-04-03', 'ACTIVE')
-- returned_at omitted — DB default kicks in if defined
```

Useful when the DB has `DEFAULT` values you want to respect.

### @Formula — Computed Fields (Read-Only)

```java
@Entity
public class Book extends PanacheEntity {
    public String title;

    @Formula("(SELECT COUNT(*) FROM book_item bi WHERE bi.book_id = id)")
    public int copyCount;

    @Formula("(SELECT COUNT(*) FROM book_lending bl WHERE bl.book_id = id AND bl.status = 'ACTIVE')")
    public int activeLendings;
}
```

`@Formula` runs a SQL subquery every time the entity is loaded. Not a column — computed on the fly. Read-only, never persisted.

### @Where — Auto-Filter (Soft Delete)

```java
@Entity
@Where(clause = "deleted = false")
public class Member extends PanacheEntity {
    public String name;
    public boolean deleted;
}
```

Every query on `Member` automatically appends `WHERE deleted = false`. Soft-deleted members become invisible without changing any query code.

### @SQLRestriction (Hibernate 6.3+ replacement for @Where)

```java
@Entity
@SQLRestriction("deleted = false")
public class Member extends PanacheEntity {
    public String name;
    public boolean deleted;
}
```

Same as `@Where` but the newer API. Use this on Quarkus 3.x / Hibernate 6.3+.

### @NaturalId — Business Key Lookups

```java
@Entity
public class Book extends PanacheEntity {
    @NaturalId
    @Column(unique = true, nullable = false, length = 13)
    public String isbn;
}
```

Hibernate caches natural ID lookups in the session:

```java
// First call → hits DB
session.byNaturalId(Book.class).using("isbn", "9780132350884").load();

// Second call in same session → cache hit, no DB query
session.byNaturalId(Book.class).using("isbn", "9780132350884").load();
```

### @Version — Optimistic Locking

```java
@Entity
public class BookItem extends PanacheEntity {
    @ManyToOne
    public Book book;

    @Version
    public int version;
}
```

Hibernate adds `WHERE version = ?` to every UPDATE:

```sql
UPDATE book_item SET book_id=1, version=2 WHERE id=5 AND version=1
```

If someone else updated the row first (version changed), the update affects 0 rows → Hibernate throws `OptimisticLockException`. No database locks needed.

### @CreationTimestamp / @UpdateTimestamp — Simpler Than @PrePersist

```java
@Entity
public class BookLending extends PanacheEntity {
    @CreationTimestamp
    @Column(updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
```

Hibernate-specific but cleaner than `@PrePersist`/`@PreUpdate` callbacks. One annotation per field, no callback methods needed.

### persistAndFlush vs persist — When It Matters

```java
repository.persist(entity);          // queued — SQL runs at transaction commit
repository.persistAndFlush(entity);  // immediate — SQL runs NOW
```

Use `persistAndFlush` when you need to:
- Catch constraint violations in a try-catch
- Get the generated ID immediately
- Ensure the INSERT happened before the next line runs

### getReference vs find — Avoid Unnecessary SELECTs

```java
// find — loads the full entity from DB
var book = entityManager.find(Book.class, 1L);  // SELECT * FROM book WHERE id = 1

// getReference — returns a proxy, NO query
var book = entityManager.getReference(Book.class, 1L);  // no SQL
```

Use `getReference` when you only need the entity to set a FK:

```java
var lending = new BookLending();
lending.book = entityManager.getReference(Book.class, bookId);  // no SELECT on book
lending.member = entityManager.getReference(Member.class, memberId);  // no SELECT on member
lendingRepository.persist(lending);  // only INSERT into book_lending
```

Three operations, one query instead of three.

### Batch Inserts

```java
@Transactional
public void importBooks(List<BookData> books) {
    for (int i = 0; i < books.size(); i++) {
        var book = new Book();
        book.title = books.get(i).title();
        book.isbn = books.get(i).isbn();
        bookRepository.persist(book);

        if (i % 50 == 0) {
            bookRepository.flush();          // send INSERTs to DB
            bookRepository.getEntityManager().clear();  // free memory
        }
    }
}
```

Without flush/clear, Hibernate keeps all entities in memory → OOM on large imports.

Configure batch size in `application.properties`:

```properties
quarkus.hibernate-orm.jdbc.statement-batch-size=50
```

### Pessimistic Locking — FOR UPDATE / NOWAIT / SKIP LOCKED

#### FOR UPDATE — Lock and Wait

Locks the row. Other transactions **wait** until the lock is released.

```java
public Optional<BookItem> findByIdForUpdate(Long id) {
    return find("id = ?1", id)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResultOptional();
}
```

```
Thread A: SELECT ... FOR UPDATE → locks row
Thread B: SELECT ... FOR UPDATE → WAITING... (blocked until A commits)
```

Safe but can block. Use when you need check + insert atomically.

#### FOR UPDATE NOWAIT — Lock or Fail Immediately

```java
public Optional<BookItem> findByIdNowait(Long id) {
    return find("id = ?1", id)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .withHint("jakarta.persistence.lock.timeout", 0)  // NOWAIT
        .firstResultOptional();
}
```

```
Thread A: SELECT ... FOR UPDATE → locks row
Thread B: SELECT ... FOR UPDATE NOWAIT → ERROR immediately
```

Fast feedback — tell the client "try again later."

#### FOR UPDATE SKIP LOCKED — The Job Queue Pattern

Skips locked rows instead of waiting. Multiple workers grab different rows simultaneously.

```java
public Optional<EmailTask> grabNext() {
    return find("status = ?1", Sort.by("id"), EmailStatus.PENDING)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .withHint("jakarta.persistence.lock.timeout", -2)  // SKIP LOCKED
        .firstResultOptional();
}
```

How it works with 3 workers on an email queue:

```
email_queue
├── id=1  PENDING
├── id=2  PENDING
├── id=3  PENDING
├── id=4  PENDING

Worker A                          Worker B                          Worker C
────────                          ────────                          ────────
SELECT ... LIMIT 1                
  FOR UPDATE SKIP LOCKED          
→ gets id=1, LOCKS it             
                                  SELECT ... LIMIT 1
                                    FOR UPDATE SKIP LOCKED
                                  → id=1 locked, SKIP
                                  → gets id=2, LOCKS it
                                                                    SELECT ... LIMIT 1
                                                                      FOR UPDATE SKIP LOCKED
                                                                    → id=1 SKIP, id=2 SKIP
                                                                    → gets id=3, LOCKS it

UPDATE status='SENDING' id=1     UPDATE status='SENDING' id=2      UPDATE status='SENDING' id=3
COMMIT                            COMMIT                            COMMIT
```

No duplicates. No waiting. Each worker grabs the next available row.

Full service example:

```java
@ApplicationScoped
public class EmailQueueService {

    @Inject EmailQueueRepository emailQueue;
    @Inject EmailSender sender;

    @Transactional
    public void processNext() {
        emailQueue.grabNext().ifPresent(task -> {
            task.status = EmailStatus.SENDING;
            sender.send(task);
            task.status = EmailStatus.SENT;
            // lock held until commit — no other worker touches this row
        });
    }
}
```

Turns a PostgreSQL table into a concurrent work queue — no Redis, no RabbitMQ needed.

| Use case | Table | Workers grab |
|---|---|---|
| Email queue | `email_queue` | Next unsent email |
| Payment processing | `payment_queue` | Next pending payment |
| Book reservation | `book_item` | Next available copy |
| Task scheduler | `scheduled_task` | Next due task |

#### Locking Summary

| Strategy | When locked | Use case |
|---|---|---|
| `FOR UPDATE` | Wait | Safe default, can block |
| `FOR UPDATE NOWAIT` | Fail immediately | Fast feedback |
| `FOR UPDATE SKIP LOCKED` | Skip to next row | Job queues, mailboxes |
| Partial unique index | Fail at commit | "Only one active" constraints |

### Partial Unique Index — DB-Level Business Rules

For rules like "only one active lending per book item":

```sql
-- Flyway migration
CREATE UNIQUE INDEX idx_one_active_lending_per_item
    ON book_lending (book_item_id)
    WHERE status = 'ACTIVE';
```

Can't express this with JPA annotations — it's a PostgreSQL feature. Goes in the migration.

Two layers working together:

```java
// Service checks first — clean sealed result
if (lendingRepository.isCurrentlyLent(bookItem)) {
    return new BookNotAvailable(bookItem.id.toString());
}

// Partial index catches race conditions at commit
// → PersistenceExceptionMapper returns 409
```

Service handles 99%. DB constraint handles the 1% race condition.

---

## Production-Grade Annotations

### @Embeddable / @Embedded — Value Objects in the Same Table

Instead of a separate table for an address:

```java
@Embeddable
public class Address {
    public String street;
    public String city;
    public String zipCode;
    public String country;
}

@Entity
public class Member extends PanacheEntity {
    public String name;

    @Embedded
    public Address address;   // stored in member table, not a separate table
}
```

Database:

```sql
member
├── id
├── name
├── street      ← from Address
├── city        ← from Address
├── zip_code    ← from Address
├── country     ← from Address
```

Override column names if needed:

```java
@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "home_street")),
    @AttributeOverride(name = "city", column = @Column(name = "home_city"))
})
public Address homeAddress;

@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "work_street")),
    @AttributeOverride(name = "city", column = @Column(name = "work_city"))
})
public Address workAddress;
```

Two addresses in the same table, different column names.

### @ElementCollection — List of Values Without a Separate Entity

```java
@Entity
public class Book extends PanacheEntity {
    public String title;

    @ElementCollection
    @CollectionTable(name = "book_tags", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "tag")
    public Set<String> tags;   // stored in book_tags table, no Tag entity needed
}
```

Database:

```sql
book_tags
├── book_id → references book(id)
└── tag     ← just a string column
```

Good for simple values (tags, roles, phone numbers). If the value needs its own ID or relationships, make it an entity instead.

### @Inheritance — Three Strategies

When entities share fields (e.g., `Payment` → `CreditCardPayment`, `BankTransferPayment`):

#### SINGLE_TABLE (default, usually best)

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment extends PanacheEntity {
    public BigDecimal amount;
    public LocalDate paidAt;
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment {
    public String cardLastFour;
}

@Entity
@DiscriminatorValue("BANK_TRANSFER")
public class BankTransferPayment extends Payment {
    public String iban;
}
```

One table, all columns, discriminator column tells Hibernate which type:

```sql
payment
├── id
├── payment_type    ← "CREDIT_CARD" or "BANK_TRANSFER"
├── amount
├── paid_at
├── card_last_four  ← null for bank transfers
├── iban            ← null for credit cards
```

Fast queries (no joins), but nullable columns for type-specific fields.

#### JOINED — One Table Per Class

```java
@Inheritance(strategy = InheritanceType.JOINED)
```

```sql
payment (id, amount, paid_at)
credit_card_payment (id → payment.id, card_last_four)
bank_transfer_payment (id → payment.id, iban)
```

Clean schema, no nulls. But every query needs a JOIN.

#### TABLE_PER_CLASS — Separate Tables, No Shared Table

```java
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
```

```sql
credit_card_payment (id, amount, paid_at, card_last_four)
bank_transfer_payment (id, amount, paid_at, iban)
```

No joins, no nulls. But polymorphic queries (`SELECT * FROM Payment`) use UNION ALL — slow.

| Strategy | Tables | Nulls | Queries | Best for |
|---|---|---|---|---|
| `SINGLE_TABLE` | 1 | Yes | Fast | Few type-specific fields |
| `JOINED` | N+1 | No | JOINs | Many type-specific fields |
| `TABLE_PER_CLASS` | N | No | UNION | Rarely query polymorphically |

### @MappedSuperclass — Share Fields Without Inheritance

When entities share fields but are NOT related polymorphically:

```java
@MappedSuperclass
public abstract class BaseEntity extends PanacheEntity {
    @CreationTimestamp
    @Column(updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    @Version
    public int version;
}

@Entity
public class Book extends BaseEntity {
    public String title;
    public String isbn;
}

@Entity
public class Member extends BaseEntity {
    public String name;
    public String email;
}
```

Both tables get `created_at`, `updated_at`, `version` — but there's no `base_entity` table. It's just code reuse, not a DB relationship.

### @Convert — Custom Type Mapping

Store a complex type as a simple column:

```java
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money == null ? null : money.amount();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal value) {
        return value == null ? null : new Money(value);
    }
}

// Entity — just use Money directly
@Entity
public class Payment extends PanacheEntity {
    @Convert(converter = MoneyConverter.class)
    public Money amount;   // stored as DECIMAL in DB, Money in Java
}
```

With `autoApply = true`, every `Money` field in every entity uses this converter automatically — no `@Convert` needed on each field.

### @SecondaryTable — One Entity, Two Tables

```java
@Entity
@Table(name = "member")
@SecondaryTable(name = "member_details", pkJoinColumns = @PrimaryKeyJoinColumn(name = "member_id"))
public class Member extends PanacheEntity {
    public String name;
    public String email;

    @Column(table = "member_details")
    public String bio;

    @Column(table = "member_details")
    public String avatarUrl;
}
```

```sql
member (id, name, email)
member_details (member_id → member.id, bio, avatar_url)
```

One entity, two tables. Useful when you have a wide table and want to split hot columns from cold columns. Hibernate JOINs them transparently.

### @Index — Create Indexes From Entities

```java
@Entity
@Table(
    name = "book_lending",
    indexes = {
        @Index(name = "idx_lending_member", columnList = "member_id"),
        @Index(name = "idx_lending_status", columnList = "status"),
        @Index(name = "idx_lending_member_status", columnList = "member_id, status")
    }
)
public class BookLending extends PanacheEntity {
    @ManyToOne public Member member;
    @Enumerated(EnumType.STRING) public LendingStatus status;
}
```

Hibernate creates these indexes during schema generation. Name them explicitly — you'll see them in query plans and error messages.

### @ColumnDefault — DB Default Values

```java
@Entity
public class BookLending extends PanacheEntity {
    @ColumnDefault("'ACTIVE'")
    @Enumerated(EnumType.STRING)
    public LendingStatus status;

    @ColumnDefault("CURRENT_DATE")
    public LocalDate borrowedAt;
}
```

Generates:

```sql
status VARCHAR(20) DEFAULT 'ACTIVE'
borrowed_at DATE DEFAULT CURRENT_DATE
```

Only affects DDL. Combine with `@DynamicInsert` so Hibernate omits these columns on INSERT and lets the DB default kick in.

### @Lob — Large Objects

```java
@Entity
public class BookReview extends PanacheEntity {
    @ManyToOne public Book book;

    @Lob
    public String content;   // TEXT in PostgreSQL (no length limit)

    @Lob
    public byte[] attachment;  // BYTEA in PostgreSQL
}
```

### Named Queries — Precompiled, Reusable

```java
@Entity
@NamedQuery(name = "Book.findByAuthor",
    query = "SELECT b FROM Book b WHERE b.author = :author ORDER BY b.title")
@NamedQuery(name = "Book.countByAuthor",
    query = "SELECT COUNT(b) FROM Book b WHERE b.author = :author")
public class Book extends PanacheEntity {
    public String title;
    public String author;
}

// In repository
public List<Book> findByAuthor(String author) {
    return find("#Book.findByAuthor", Parameters.with("author", author)).list();
}
```

Named queries are validated at startup — typos in field names fail fast instead of at runtime.

### Entity Graphs — Control What Gets Loaded

```java
@Entity
@NamedEntityGraph(
    name = "BookLending.withBookAndMember",
    attributeNodes = {
        @NamedAttributeNode("book"),
        @NamedAttributeNode("member")
    }
)
public class BookLending extends PanacheEntity {
    @ManyToOne(fetch = FetchType.LAZY) public Book book;
    @ManyToOne(fetch = FetchType.LAZY) public Member member;
}

// In repository — load lending WITH book and member in one query
public Optional<BookLending> findByIdWithDetails(Long id) {
    return find("id = ?1", id)
        .withHint("jakarta.persistence.fetchgraph", 
            entityManager.getEntityGraph("BookLending.withBookAndMember"))
        .firstResultOptional();
}
```

Entity graphs override fetch types per query. Keep entities lazy by default, eagerly load only when needed via graphs.

## Quick Reference

```
Parent ←──── Child
(Book)       (BookItem)

Child has:   @ManyToOne Book book          → creates FK
Parent has:  @OneToMany(mappedBy) Set<>    → optional mirror, no FK

Owner = whoever has @ManyToOne = whoever has the FK column
```
