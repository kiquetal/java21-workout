# AI Coding Rules for Book Lending Repo (Java 21+ & Quarkus)

When assisting with this repository, the AI must strictly adhere to the following architectural, framework, and domain modeling rules:

## 1. Architectural Boundaries & Data Flow
*   **"Parse, Don't Validate":** Domain models (e.g., `Isbn`, `MemberId`) must validate themselves upon construction. Invalid states must be unrepresentable.
*   **Two-Step Parsing:** 
    1. REST Resources receive raw data via DTO records (in `dto/`) validated with Bean Validation (`@NotNull`, etc.).
    2. The Resource parses DTOs into strongly-typed Domain Commands (in `domain/`) using the Value Types.
*   **Service Layer Isolation:** Services (in `service/`) must ONLY accept validated Domain Commands. They must never see raw primitives (like `String` or `Long`) or DTOs.
*   **No Exceptions for Business Logic:** Expected failures (e.g., "Book Not Found") must be modeled as a `sealed interface` Result type (e.g., `LendingResult.Success`, `LendingResult.BookNotAvailable`). 
*   **Exhaustive Pattern Matching:** REST Resources must translate domain results to HTTP responses using an exhaustive `switch` statement with pattern matching.

## 2. JPA & Hibernate ORM Correctness
*   **Entities are Classes:** JPA Entities must be mutable `class`es extending `PanacheEntity` with no-arg constructors. **Never use `record`s for entities.**
*   **ID Generation in Flyway:** Do not use `BIGSERIAL` in Flyway scripts. Panache expects `BIGINT` with a `<TABLE>_SEQ` sequence incrementing by 50 for batched inserts.
*   **Dirty Checking vs Persist:** 
    *   Use `persist()` ONLY for creating new entities.
    *   For updates, rely on Hibernate's dirty checking on managed entities. Do not call `persist()`.
*   **Relationship Ownership:** The `@ManyToOne` side owns the foreign key.
*   **Type Witnesses:** Always use type witnesses with Panache `Optional` returns (e.g., `Book.<Book>findByIdOptional()`) to avoid `Optional<Object>` inference issues.

## 3. Concurrency & Locking
*   **The "Fitting Room" Rule:** If you are validating a business rule against a database row (e.g., checking if a user has overdue books before a new loan), you MUST lock that row (`PESSIMISTIC_WRITE`) to prevent concurrent transactions from invalidating the rule.

## 4. Testing Strategy
*   **Flat Test Structure:** Keep one test class per concept (e.g., `IsbnTest`, `LendingServiceTest`). **Do not use `@Nested` classes.**
*   **Pure Unit Tests for Domain:** Value Types (e.g., `BookId`, `Email`) and Sealed Results must be tested with plain, lightning-fast `@Test` methods. No mocks, no DB.
*   **Integration Tests for Repositories/Resources:** Use `@QuarkusTest` ONLY for Integration Tests that require the Testcontainers PostgreSQL database (via Dev Services).
