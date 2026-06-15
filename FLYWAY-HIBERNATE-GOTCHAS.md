# Flyway + Hibernate — Naming Gotchas

When Flyway owns the schema and Hibernate validates it, the SQL must match Hibernate's expectations exactly.

## Table Names

Hibernate uses the **entity class name** as the table name by default:
- `Book` entity → table `Book` (not `book`)
- `BookItem` entity → table `BookItem` (not `book_item`)
- `Member` entity → table `Member` (not `member`)

Exception: if you use `@Table(name = "book_lending")`, that overrides it.

## Column Names

Hibernate uses the **field name** as-is (no snake_case conversion by default):
- `publishDate` field → column `publishDate` (not `publish_date`)
- `createdAt` field → column `createdAt` (not `created_at`)
- `creationDate` field → column `creationDate` (not `creation_date`)

Exception: `@Column(name = "borrowed_at")` overrides it.

## ID Strategy — Sequences, Not BIGSERIAL

PanacheEntity uses `SEQUENCE` strategy by default. You need:
```sql
CREATE SEQUENCE Book_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE BookItem_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE Member_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE book_lending_SEQ START WITH 1 INCREMENT BY 50;
```

Pattern: `<TableName>_SEQ` with increment 50 (Hibernate's default allocation size).

Do NOT use `BIGSERIAL` — Hibernate won't know about the auto-increment and will try to use the sequence.

## Enum Column Width

`@Enumerated(EnumType.STRING)` without `@Column(length=X)` defaults to `VARCHAR(255)`.
If your migration uses `VARCHAR(20)`, Hibernate may complain or truncate.

## CHECK Constraints

Hibernate generates CHECK constraints from `@Enumerated`. Your migration must list the same values:
```sql
CHECK (status IN ('AVAILABLE', 'LENT', 'UNAVAILABLE', 'DAMAGED'))
```

If you add a new enum value, update the migration OR use `VARCHAR(255)` without CHECK (let the app validate).

## Quick Checklist

| Entity field | Migration must have |
|---|---|
| Class name `Book` | Table `Book` |
| Field `publishDate` | Column `publishDate` |
| `@Table(name = "x")` | Table `x` |
| `@Column(name = "x")` | Column `x` |
| PanacheEntity | Sequence `<Table>_SEQ` increment 50 |
| `@Enumerated(STRING)` | `VARCHAR(255)` or match length exactly |
| `@JoinColumn(name = "x")` | Column `x` as FK |

## How to Debug

If Hibernate validation fails on startup, read the suggested SQL it prints — it tells you exactly what's missing or mismatched. Copy it into your migration (after verifying).
