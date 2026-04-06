# Book Lending — Java 21+ Domain Modeling Playground

A Quarkus project for practicing modern Java domain modeling: sealed types, records, pattern matching, and type-driven design.

## Quick Start

```bash
cd book-lending
./mvnw quarkus:dev
```

Dev Services auto-starts PostgreSQL. Flyway runs migrations. Hit `http://localhost:8080/q/dev-ui` for the Dev UI.

```bash
# Lend a book
curl -X POST http://localhost:8080/api/lendings \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1, "dueDate": "2026-04-30"}'
```

## Project Structure

```
src/main/java/dev/learning/
├── domain/          # Entities, sealed types, value records (core model)
├── dto/             # Request/response records (API boundary)
├── service/         # Business logic, returns sealed results
├── resource/        # JAX-RS endpoints, pattern match → HTTP
└── repository/      # Panache repositories (when needed)
```

## Key Concepts Practiced

### 1. Schema Migrations with Flyway

Flyway owns the schema — Hibernate DDL generation is disabled.

```properties
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate-at-start=true
```

Versioned SQL files in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql   # Tables, constraints, indexes
V2__seed_data.sql        # Dev data
```

**Dev UI shortcut**: When starting fresh, the Quarkus Dev UI (`/q/dev-ui`) has a Flyway card with a "Create Initial Migration" button that generates DDL from your entities.

### 2. PanacheEntity Uses Sequences, Not BIGSERIAL

This is a common trap when writing migrations by hand. PostgreSQL has two ways to auto-generate IDs:

**BIGSERIAL (identity)** — the database picks the ID on INSERT:
```sql
-- ❌ What you might write by hand
CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,   -- DB assigns 1, 2, 3, 4...
    title VARCHAR(255) NOT NULL
);
```

**BIGINT + SEQUENCE** — Hibernate picks the ID before INSERT:
```sql
-- ✅ What Panache actually expects
CREATE SEQUENCE book_SEQ START WITH 1 INCREMENT BY 50;
CREATE TABLE book (
    id BIGINT NOT NULL PRIMARY KEY,   -- Hibernate assigns from sequence
    title VARCHAR(255) NOT NULL
);
```

Why `INCREMENT BY 50`? Hibernate pre-fetches IDs in batches. One `SELECT nextval('book_SEQ')` gives it 50 IDs to use in memory — no DB roundtrip per insert.

```
BIGSERIAL:                          SEQUENCE (Panache):
INSERT → DB picks id=1             nextval('book_SEQ') → 1 (gets 1-50)
INSERT → DB picks id=2             INSERT with id=1  (no DB call)
INSERT → DB picks id=3             INSERT with id=2  (no DB call)
...                                 ...
(roundtrip every insert)            INSERT with id=50 (no DB call)
                                    nextval('book_SEQ') → 51 (gets 51-100)
```

`PanacheEntity` uses `GenerationType.SEQUENCE` internally — you can't change it without overriding the `@Id` field. The naming convention is `<TABLE>_SEQ`:

| Entity | Table | Sequence Panache expects |
|---|---|---|
| `Book` | `book` | `book_SEQ` |
| `Member` | `member` | `member_SEQ` |
| `BookLending` | `book_lending` | `book_lending_SEQ` |

**How to avoid this mistake**: Use the entity-first workflow. Write your entities, set `database.generation=drop-and-create`, run `quarkus dev`, and check the Dev UI → Hibernate ORM card for the generated DDL. It will show you the sequences. Copy that SQL into your Flyway migration, then switch to `database.generation=none`.

### 3. Entities Are Classes, Not Records

Hibernate requires mutable classes with no-arg constructors. Records can't be entities:

```java
// ❌ Records are immutable, final, no no-arg constructor
@Entity
public record Book(Long id, String title) {}

// ✅ Entities must be mutable Panache classes
@Entity
public class Book extends PanacheEntity {
    public String title;
    public String isbn;
}
```

### 4. Records for Everything Else

| Use case | Example |
|---|---|
| DTOs | `record LendRequest(Long bookId, ...)` |
| Value types | `record Isbn(String value)` |
| Sealed result variants | `record Success(BookLending lending)` |
| Panache projections | `record BookSummary(String title, String author)` |
| Commands | `record LendCommand(Isbn isbn, ...)` |

### 5. Sealed Types as Result Types (Not Exceptions)

Model business outcomes explicitly — no exceptions for expected failures:

```java
public sealed interface LendingResult {
    record Success(BookLending lending) implements LendingResult {}
    record BookNotAvailable(String isbn) implements LendingResult {}
    record MemberNotFound(Long memberId) implements LendingResult {}
}
```

This is Java's equivalent of F#'s discriminated unions / `Result<'T, 'E>`. Domain-specific sealed types are preferred over a generic `Result<T>` because:

- Each failure variant carries its own typed data
- The compiler enforces exhaustive handling
- Adding a new variant breaks all unhandled switches at compile time
- Variant names carry business meaning

### 6. Pattern Matching in the Resource

The resource translates domain results to HTTP — one exhaustive switch:

```java
return switch (result) {
    case Success(var lending) -> Response.ok(toResponse(lending)).build();
    case BookNotAvailable(var isbn) -> Response.status(409).entity(new ErrorResponse("...")).build();
    case MemberNotFound(var id) -> Response.status(404).entity(new ErrorResponse("...")).build();
};
```

### 7. Parse, Don't Validate (F#-style Value Types)

Validate at construction — make invalid states unrepresentable:

```java
public record Isbn(String value) {
    public Isbn {
        if (value == null || !value.matches("\\d{13}"))
            throw new IllegalArgumentException("Invalid ISBN: " + value);
    }
}
```

Once an `Isbn` exists, it's guaranteed valid. The domain only speaks in typed values, never raw strings.

### 8. DTO ↔ Domain Boundary — Why Can't It Be Like F#?

In F#, you receive raw data and parse it directly into a domain type in one step:

```fsharp
// F# — the request IS the parsing step, one type does both jobs
type LendCommand = {
    Isbn: Isbn           // already validated on construction
    MemberId: MemberId   // already validated on construction
    DueDate: DateTime
}

// JSON deserializer calls Isbn.create("978...") which returns Result<Isbn, Error>
// If it fails, you never get a LendCommand at all
```

In Java, you **can't do this** because of Jackson (the JSON deserializer). Jackson needs:
- A no-arg constructor OR a constructor with **simple types** (String, Long, etc.)
- It doesn't know how to call `new Isbn("978...")` with validation inside a compact constructor
- If the `Isbn` constructor throws, Jackson gives you a generic 400 error with no useful message

So you're forced into two steps:

```
Step 1: JSON → DTO (raw types, Jackson can handle this)
Step 2: DTO → Domain types (you parse and validate here)
```

Concretely:

```java
// STEP 1 — dto/LendRequest.java
// Jackson deserializes JSON into this. Simple types only.
public record LendRequest(
    @NotBlank String isbn,       // just a String — Jackson is happy
    @NotNull Long memberId,      // just a Long — Jackson is happy
    @NotNull LocalDate dueDate
) {}

// STEP 2 — resource parses DTO into domain types
@POST
public Response lend(@Valid LendRequest request) {
    // This is the F# "parse" moment — raw strings become domain types
    var command = new LendCommand(
        new Isbn(request.isbn()),           // validates here
        new MemberId(request.memberId()),   // validates here
        request.dueDate()
    );
    var result = lendingService.lend(command);
    // ...
}

// domain/LendCommand.java — the service only sees this
public record LendCommand(Isbn isbn, MemberId memberId, LocalDate dueDate) {}
```

The service receives `LendCommand` with **already-validated domain types**:

```java
// Service never sees raw strings. Isbn and MemberId are guaranteed valid.
public LendingResult lend(LendCommand command) {
    // command.isbn() → Isbn, not String
    // command.memberId() → MemberId, not Long
    // impossible to have invalid data here
}
```

**Why F# doesn't have this problem**: F#'s serializers (like Thoth.Json) support custom decoders — you write a function that parses `string → Result<Isbn, Error>` and wire it into deserialization. The parsing IS the deserialization. Java's Jackson doesn't work that way — it constructs objects first, validates later.

**The mental model**:

```
F#:    JSON ──parse──→ Domain type (one step, decoder does validation)
Java:  JSON ──jackson──→ DTO (dumb data) ──you parse──→ Domain type (two steps)
```

The resource layer is where the two-step gap lives. It's the cost of using Jackson. Everything after that boundary is the same as F# — validated types, no raw strings, no nulls.

**The flow through layers**:

```
Client JSON → LendRequest (DTO, raw)
            → LendCommand (domain, validated)     ← parsing boundary
            → LendingService (works with domain)
            → LendingResult (sealed outcome)
            → LendingResponse (DTO, shaped for client)
            → Client JSON
```

- **DTOs** live in `dto/`, face the outside world, carry validation annotations
- **Domain types** live in `domain/`, guaranteed valid after construction
- **The resource** is the translator — the parsing boundary
- **The service** never sees DTOs, never knows about HTTP

### 9. Validation Layers

| Layer | Purpose |
|---|---|
| Bean Validation on DTOs | Early, user-friendly error messages (`@NotNull`, `@Size`) |
| Value type constructors | Domain integrity (`Isbn`, `Email`) |
| `@Column` annotations | Documents entity-to-table mapping |
| DB constraints (Flyway SQL) | Last line of defense — always there |

### 10. Panache Type Witness Quirk

`findByIdOptional` returns `Optional<Object>`. Use a type witness to get the right type:

```java
// ❌ Returns Optional<Object>
Member.findByIdOptional(id)

// ✅ Returns Optional<Member>
Member.<Member>findByIdOptional(id)
```

### 11. Java Type Inference in Optional Chains

When chaining `map`/`orElseGet` with sealed types, Java infers the type from the first branch and locks it. Use a type witness on `map` to widen:

```java
// ❌ Compiler infers map returns Optional<Success>, orElseGet fails
.map(book -> new LendingResult.Success(lending))
.orElseGet(() -> new LendingResult.BookNotAvailable(isbn))

// ✅ Type witness tells compiler to use the parent type
.<LendingResult>map(book -> new LendingResult.Success(lending))
.orElseGet(() -> new LendingResult.BookNotAvailable(isbn))
```

### 12. Hibernate Dirty Checking — `persist()` vs Managed Entities

When you **create** a new entity, you must call `persist()` — Hibernate doesn't know about it yet:

```java
var member = new Member();              // just a Java object, not in DB
member.name = command.name();
member.email = command.email().value();
memberRepository.persist(member);       // NOW Hibernate tracks it and saves to DB
```

When you **update** an existing entity, Hibernate already loaded it — it's "managed". Any field change is auto-flushed at transaction commit:

```java
memberRepository.findByMemberId(...)    // loads from DB — Hibernate "manages" it
    .map(member -> {
        member.name = command.name();   // mutate the managed entity
        member.email = ...;             // Hibernate tracks these changes
        // no persist() needed — auto-flushed at commit
        return new Success(member);
    })
```

| | Create | Update |
|---|---|---|
| Entity comes from | `new Entity()` — not managed | `repository.find()` — managed |
| Needs `persist()`? | Yes | No — dirty checking handles it |
| Needs `@Transactional`? | Yes | Yes |

"Managed" = Hibernate loaded it and is watching it. Change a field → Hibernate detects the diff → flushes the UPDATE at commit. No explicit save needed.

### 13. Test Configuration — Separate `application.properties`

Quarkus looks for `src/test/resources/application.properties` and uses it **instead of** the main one during tests. This lets you isolate test settings without polluting your dev/prod config.

**Why bother?** Without it, your tests inherit whatever's in the main config. If you enable Flyway, switch Hibernate to `validate`, or change logging in main — your tests break. A separate test config keeps them independent.

**File layout:**

```
src/main/resources/application.properties    ← dev + prod
src/test/resources/application.properties    ← tests only (overrides main)
```

**What to put in the test config:**

```properties
# ── Dev Services (test container) ──
# Quarkus auto-starts a PostgreSQL container for tests.
# These settings control THAT container, not your real DB.
quarkus.datasource.db-kind=postgresql
quarkus.devservices.enabled=true
quarkus.datasource.devservices.image-name=postgres:17       # pin version
quarkus.datasource.devservices.db-name=book_lending_test    # distinct name
quarkus.datasource.devservices.username=test
quarkus.datasource.devservices.password=test

# ── Hibernate ──
# drop-and-create = clean schema every test run
quarkus.hibernate-orm.schema-management.strategy=drop-and-create
quarkus.hibernate-orm.log.sql=true    # see queries in test output

# ── Flyway ──
# Off — Hibernate manages schema via drop-and-create
quarkus.flyway.migrate-at-start=false
```

**Key differences from main config:**

| Setting | Main (dev/prod) | Test |
|---|---|---|
| Hibernate schema | `%dev.drop-and-create` / `%prod.none` | `drop-and-create` (always clean) |
| Flyway | Depends on environment | Off — Hibernate owns the schema |
| Dev Services image | Default (latest) | Pinned (`postgres:17`) for reproducibility |
| DB name | Default | `book_lending_test` — distinct from dev |
| SQL logging | Off | On — useful for debugging test failures |

**How it works at runtime:**

```
./mvnw quarkus:dev  → src/main/resources/application.properties (%dev profile)
./mvnw test         → src/test/resources/application.properties (overrides main)
```

Both spin up a Testcontainers PostgreSQL via Dev Services, but with independent settings. The test container is ephemeral — created before tests, destroyed after.

**Test types and what they need:**

| Test type | Needs `@QuarkusTest`? | Needs Dev Services? | Example |
|---|---|---|---|
| Pure domain (value types, sealed results) | No | No | `ValueTypeTest`, `SealedResultTest` |
| Integration (REST endpoints) | Yes | Yes (auto) | `BookItemResourceTest` |

Pure domain tests don't touch the container at all — they run in milliseconds. Only `@QuarkusTest` classes trigger Dev Services and the full Quarkus lifecycle.

## Tech Stack

- Java 21+
- Quarkus 3.34
- Hibernate ORM with Panache
- RESTEasy Reactive + Jackson
- PostgreSQL (via Dev Services)
- Flyway
- Hibernate Validator
