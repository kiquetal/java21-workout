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

## Quick Reference

```
Parent ←──── Child
(Book)       (BookItem)

Child has:   @ManyToOne Book book          → creates FK
Parent has:  @OneToMany(mappedBy) Set<>    → optional mirror, no FK

Owner = whoever has @ManyToOne = whoever has the FK column
```
