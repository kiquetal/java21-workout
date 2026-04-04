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

## Quick Reference

```
Parent ←──── Child
(Book)       (BookItem)

Child has:   @ManyToOne Book book          → creates FK
Parent has:  @OneToMany(mappedBy) Set<>    → optional mirror, no FK

Owner = whoever has @ManyToOne = whoever has the FK column
```
