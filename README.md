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

### 2. Entities Are Classes, Not Records

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

### 3. Records for Everything Else

| Use case | Example |
|---|---|
| DTOs | `record LendRequest(Long bookId, ...)` |
| Value types | `record Isbn(String value)` |
| Sealed result variants | `record Success(BookLending lending)` |
| Panache projections | `record BookSummary(String title, String author)` |
| Commands | `record LendCommand(Isbn isbn, ...)` |

### 4. Sealed Types as Result Types (Not Exceptions)

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

### 5. Pattern Matching in the Resource

The resource translates domain results to HTTP — one exhaustive switch:

```java
return switch (result) {
    case Success(var lending) -> Response.ok(toResponse(lending)).build();
    case BookNotAvailable(var isbn) -> Response.status(409).entity(new ErrorResponse("...")).build();
    case MemberNotFound(var id) -> Response.status(404).entity(new ErrorResponse("...")).build();
};
```

### 6. Parse, Don't Validate (F#-style Value Types)

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

### 7. DTO ↔ Domain Boundary

```
Client JSON → DTO (request) → Service (domain) → DTO (response) → Client JSON
```

- **DTOs** live in `dto/`, face the outside world, carry validation annotations
- **Entities** stay internal, never leak to the API
- **The resource** is the translator between the two worlds
- **The service** never sees DTOs, never knows about HTTP

### 8. Validation Layers

| Layer | Purpose |
|---|---|
| Bean Validation on DTOs | Early, user-friendly error messages (`@NotNull`, `@Size`) |
| Value type constructors | Domain integrity (`Isbn`, `Email`) |
| `@Column` annotations | Documents entity-to-table mapping |
| DB constraints (Flyway SQL) | Last line of defense — always there |

### 9. Panache Type Witness Quirk

`findByIdOptional` returns `Optional<Object>`. Use a type witness to get the right type:

```java
// ❌ Returns Optional<Object>
Member.findByIdOptional(id)

// ✅ Returns Optional<Member>
Member.<Member>findByIdOptional(id)
```

### 10. Java Type Inference in Optional Chains

When chaining `map`/`orElseGet` with sealed types, Java infers the type from the first branch and locks it. Use a type witness on `map` to widen:

```java
// ❌ Compiler infers map returns Optional<Success>, orElseGet fails
.map(book -> new LendingResult.Success(lending))
.orElseGet(() -> new LendingResult.BookNotAvailable(isbn))

// ✅ Type witness tells compiler to use the parent type
.<LendingResult>map(book -> new LendingResult.Success(lending))
.orElseGet(() -> new LendingResult.BookNotAvailable(isbn))
```

## Tech Stack

- Java 21+
- Quarkus 3.34
- Hibernate ORM with Panache
- RESTEasy Reactive + Jackson
- PostgreSQL (via Dev Services)
- Flyway
- Hibernate Validator
