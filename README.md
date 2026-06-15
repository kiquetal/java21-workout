# Book Lending — Java 21+ Functional Domain Modeling

A Quarkus project for practicing modern Java with a **functional mindset**: sealed types as control flow, records as immutable data, pattern matching as dispatch, and railway-oriented composition for error handling.

## Quick Start

```bash
./mvnw quarkus:dev
```

Dev Services auto-starts PostgreSQL. Flyway runs migrations. Hit `http://localhost:8080/chiron/q/dev-ui` for the Dev UI.

```bash
curl -X POST http://localhost:8080/chiron/api/lendings \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1}'
```

## The Functional Approach

### Why Functional in Java?

| Imperative habit | Functional replacement | Benefit |
|------------------|----------------------|---------|
| Throw exceptions for business failures | Return sealed result types | Compiler enforces handling |
| Null checks with early returns | `Optional` chains / `Either` | No forgotten paths |
| Mutable DTOs | Immutable records | Construct once, trust everywhere |
| `if-else` trees | Pattern matching on sealed types | Exhaustive, concise |
| Try-catch at every layer | Railway composition (`flatMap`) | Errors flow to one exit point |

### The Three Pillars

**1. Sealed types = "one of these"** (sum types)

```java
sealed interface LendingResult {
    record Success(LendingDetail book) implements LendingResult {}
    record MemberNotFound(MemberId id) implements LendingResult {}
    record AlreadyLent(LendingDetail detail) implements LendingResult {}
    record MaximumLimitReached(MemberId id) implements LendingResult {}
}
```

**2. Records = "all of these together"** (product types)

```java
record LendCommand(BookItemId bookItemId, MemberId memberId) {}
record LendingDetail(BookItemId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
```

**3. Either + flatMap = railway composition**

```java
return findMember(cmd)
    .flatMap(this::checkOverdue)
    .flatMap(this::checkMaximumLentNumber)
    .flatMap(m -> findBookItemAndMember(cmd, m))
    .flatMap(this::checkIfAlreadyLent)
    .fold(err -> err, this::persistAndReturnResult);
```

Each step can succeed (Right) or fail (Left). First failure stops the chain — error flows to `fold`.

### When This Style Helps vs Doesn't

| ✅ Use it when | ❌ Don't force it when |
|---------------|----------------------|
| Multi-step validation pipelines | Simple CRUD with no rules |
| Multiple distinct error types from one operation | The method can't fail |
| Data transformation (DTO ↔ Domain) | Performance-critical tight loops |
| Making illegal states unrepresentable | Team hasn't learned the pattern yet |

### Functional vs Concurrency — Different Axes

```
┌─────────────────────────────────┐
│     What to compute             │  ← Functional (Either, sealed types, flatMap)
│  (logic, data flow, errors)     │
└─────────────────────────────────┘
              ↕  orthogonal
┌─────────────────────────────────┐
│     How to execute it           │  ← Concurrency (virtual threads, StructuredTaskScope)
│  (parallelism, threading)       │
└─────────────────────────────────┘
```

- **Sequential dependencies** (step N needs step N-1) → functional pipeline
- **Independent fan-out** (fetch A, B, C in parallel) → `StructuredTaskScope`
- **Blocking I/O on hot paths** → `@RunOnVirtualThread`

They compose — a pipeline runs on a virtual thread, and parallel results feed into `flatMap` chains.

---

## Project Structure

```
src/main/java/dev/learning/
├── domain/
│   ├── entity/      # Panache entities (classes — Hibernate requirement)
│   ├── type/        # Value types — validated records (BookItemId, Email, Isbn, Either)
│   ├── command/     # Action records (LendCommand, CreateMemberCommand)
│   └── result/      # Sealed result interfaces (LendingResult, BookItemResult)
├── repository/      # Panache repositories — all DB access
├── service/         # Business logic — pipelines that return sealed results
├── resource/        # JAX-RS endpoints — parse DTOs, pattern match results → HTTP
└── dto/             # Request/response records — API boundary
```

### Layer Rules

- **Services** never call entity statics — always go through repositories
- **Resources** parse DTOs → domain commands, pattern match sealed results → HTTP
- **Repositories** accept/return domain types (`findByMemberId(MemberId)`, not `findById(Long)`)
- **Entities** are classes; everything else is records

### Service Method Shape

Every service method follows:

```
Command (validated) → Pipeline (Either chain) → Sealed Result
```

1. **Parse** — Resource converts DTO → Command with value types
2. **Decide** — Service runs pipeline: reads via repo, applies rules
3. **Execute** — Final step persists (the only side effect)
4. **Respond** — Resource pattern-matches result → HTTP response

Side effects happen only at the end. Everything before is a decision.

---

## Patterns & Learnings

### Parse, Don't Validate

Validate at construction — make invalid states unrepresentable:

```java
public record Isbn(String value) {
    public Isbn {
        if (value == null || !value.matches("\\d{13}"))
            throw new IllegalArgumentException("Invalid ISBN: " + value);
    }
}
```

Once an `Isbn` exists, it's guaranteed valid. The domain never sees raw strings.

### DTO ↔ Domain Boundary

Jackson needs simple types. So you're forced into two steps (unlike F# where parsing IS deserialization):

```
JSON ──jackson──→ DTO (raw types) ──resource parses──→ Command (domain types)
```

```java
@POST
public Response register(@Valid LendRequest request) {
    var command = new LendCommand(
        new BookItemId(request.bookId()),   // validates here
        new MemberId(request.memberId())    // validates here
    );
    var result = lendingService.lend(command);
    return switch (result) {
        case Success(var detail) -> Response.ok(detail).build();
        case MemberNotFound(var id) -> Response.status(404).entity(...).build();
        // exhaustive — compiler enforces all cases
    };
}
```

### Pattern Matching as Dispatch

No `default` needed on sealed types — the compiler guarantees exhaustiveness:

```java
return switch (result) {
    case LendingResult.Success(var detail) -> Response.ok(detail).build();
    case LendingResult.AlreadyLent(var detail) -> Response.status(409).entity(...).build();
    case LendingResult.MemberNotFound(var id) -> Response.status(404).entity(...).build();
    case LendingResult.BookNotFound(var id) -> Response.status(404).entity(...).build();
    case LendingResult.MemberHasOverdueBooks(var id, var books) -> Response.status(403).entity(...).build();
    case LendingResult.MaximumLimitReached(var id) -> Response.status(403).entity(...).build();
};
```

### `@Transactional` on the Service, Not the Resource

```
Resource (no TX)          Service (@Transactional)
  parse DTO                 TX BEGIN
  call service ──────────→   repo.find() → managed entity
  pattern match result ←──   business rules + persist
  build HTTP response        TX COMMIT (auto-flush)
```

Entities live and die inside the transaction. What escapes is an immutable record inside a sealed result.

---

## Infrastructure Notes

### Flyway Migrations

Flyway owns the schema — Hibernate DDL generation is disabled.

```properties
quarkus.hibernate-orm.schema-management.strategy=none
quarkus.flyway.migrate-at-start=true
```

### PanacheEntity Uses Sequences, Not BIGSERIAL

Panache uses `GenerationType.SEQUENCE` with `INCREMENT BY 50` (batch allocation). Your migrations need:

```sql
CREATE SEQUENCE book_SEQ START WITH 1 INCREMENT BY 50;
CREATE TABLE book (
    id BIGINT NOT NULL PRIMARY KEY,
    ...
);
```

| Entity | Sequence expected |
|--------|------------------|
| `Book` | `book_SEQ` |
| `Member` | `member_SEQ` |
| `BookLending` | `book_lending_SEQ` |
| `BookItem` | `bookitem_SEQ` |

### Hibernate Dirty Checking

- **New entity** → must call `persist()`
- **Loaded entity** → mutate fields, auto-flushed at commit (no explicit save)

### Test Configuration

`src/test/resources/application.properties` overrides main config:
- `drop-and-create` for clean schema each run
- Flyway off (Hibernate owns schema in tests)
- Pinned Postgres image for reproducibility

---

## Related Docs

| Doc | Topic |
|-----|-------|
| [EITHER-COMPOSITION.md](EITHER-COMPOSITION.md) | Deep dive on Either, map vs flatMap, railway pattern |
| [FUNCTIONAL-MINDSET.md](FUNCTIONAL-MINDSET.md) | When and why to use functional style, trade-offs |
| [PESSIMISTIC-LOCKING.md](PESSIMISTIC-LOCKING.md) | Concurrent access patterns |
| [FLYWAY-HIBERNATE-GOTCHAS.md](FLYWAY-HIBERNATE-GOTCHAS.md) | Migration pitfalls |
| [JPA-TRICKS.md](JPA-TRICKS.md) | Hibernate/Panache patterns |

## Tech Stack

- Java 21+
- Quarkus 3.34
- Hibernate ORM with Panache
- RESTEasy Reactive + Jackson
- PostgreSQL (via Dev Services)
- Flyway
- Micrometer + Prometheus
