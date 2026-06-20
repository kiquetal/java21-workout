# 🛤️ Java 21+ Functional Programming Learning Roadmap

Welcome to your functional learning roadmap! This document serves as a centralized syllabus, checklist, and navigation guide for mastering **Modern Functional Java 21+** within this codebase.

---

## 📊 Learning Progress Checklist

- [ ] **Milestone 1: Parse, Don't Validate (Boundary Safety)**
  - [x] Read theory on value-driven modeling.
  - [ ] Implement custom validated value record types.
- [ ] **Milestone 2: Railway-Oriented Programming (Monadic Pipelines)**
  - [x] Read theory on `Either<L, R>` and linear pipelines.
  - [ ] Implement step-by-step `flatMap` chains and use `.fold(...)` as the exit ramp.
- [ ] **Milestone 3: Sealed Types as Control Flow (Sum Types & Exhaustive Matching)**
  - [x] Model outcomes using `sealed interface` and `record` variants.
  - [ ] Write exhaustive pattern-matching switches with zero default cases.
- [ ] **Milestone 4: Transaction Isolation & Concurrency Safety**
  - [x] Separate mutable JPA entity lifetimes from immutable domain records.
  - [ ] Implement pessimistic locking to secure concurrent "check-then-act" operations.
- [ ] **Milestone 5: Advanced Java 21+ Concurrency (Virtual Threads & Structured Concurrency)**
  - [x] Understand lightweight virtual threads and structured scope theories.
  - [ ] Leverage virtual thread scheduling (`@RunOnVirtualThread`) and parallel subtasks (`StructuredTaskScope`).

---

## 🗺️ Detailed Milestones

### 📍 Milestone 1: "Parse, Don't Validate"
Ensure that invalid states are completely unrepresentable before they reach your business logic.

*   **The Idea:** Instead of validating raw `String` or `Long` variables at every method, convert them at the controller boundary into strongly typed validated records (value types) like `Isbn` or `MemberId`.
*   **The Two-Step Parsing Flow:**
    ```
    JSON Input ──► Jackson DTO (Raw Types) ──► Resource Parses ──► Command (Validated Value Types)
    ```
*   **Key Files to Study:**
    *   [`MY-LEARNINGS-BLOG.md#L11-L54`](file:///mydata/codes/2026/java21-workout/MY-LEARNINGS-BLOG.md#L11-L54) — Parsing boundaries and "Jackson Tax".
    *   [`README.md#L134-L149`](file:///mydata/codes/2026/java21-workout/README.md#L134-L149) — Value record validation examples.
    *   [`src/main/java/dev/learning/domain/type/BookItemId.java`](file:///mydata/codes/2026/java21-workout/src/main/java/dev/learning/domain/type/BookItemId.java) — A real, validated value record.

---

### 📍 Milestone 2: Railway-Oriented Programming (`Either<L, R>`)
Avoid using checked exceptions for expected domain failures. Model your business flows as sequential data transformations.

*   **The Idea:** An operation can succeed on the **Right** track (returning the success value) or fail on the **Left** track (short-circuiting the flow with a typed error).
*   **Railway Pipeline Composition:**
    ```
    Step 1 (Right) ──► Step 2 (Right) ──► Step 3 (Right) ──► Success Result
       │                  │                  │
       ▼ (Left Error)     ▼ (Left Error)     ▼ (Left Error)
    Short Circuit ─────► Short Circuit ─────► Short Circuit ──► Error Result (.fold)
    ```
*   **Key Files to Study:**
    *   [`EITHER-COMPOSITION.md`](file:///mydata/codes/2026/java21-workout/EITHER-COMPOSITION.md) — Comprehensive guide to Either, mapping, and folding.
    *   [`src/main/java/dev/learning/domain/type/Either.java`](file:///mydata/codes/2026/java21-workout/src/main/java/dev/learning/domain/type/Either.java) — The custom monadic Either interface.

---

### 📍 Milestone 3: Sealed Types as Control Flow (Exhaustive Switches)
Leverage the Java compiler to guarantee that all potential use-case outcomes are handled explicitly.

*   **The Idea:** Services return custom `sealed interface` outcomes with specific record variants. REST Controllers match on these variants using Java 21 `switch` expressions without `default` blocks.
*   **Pattern Matching Dispatch:**
    ```java
    return switch (result) {
        case Success(var detail) -> Response.ok(detail).build();
        case MemberNotFound(var id) -> Response.status(404).entity(...).build();
        case AlreadyLent(var detail) -> Response.status(409).entity(...).build();
    }; // The compiler throws an error if any case is missing!
    ```
*   **Key Files to Study:**
    *   [`FUNCTIONAL-MINDSET.md#L14-L38`](file:///mydata/codes/2026/java21-workout/FUNCTIONAL-MINDSET.md#L14-L38) — Pillar description and code samples.
    *   [`src/main/java/dev/learning/domain/result/LendingResult.java`](file:///mydata/codes/2026/java21-workout/src/main/java/dev/learning/domain/result/LendingResult.java) — Example of a sealed result type.

---

### 📍 Milestone 4: Concurrency & Bounded Transactions
Isolate transactions securely and handle high-concurrency races elegantly.

*   **The Idea:**
    *   Keep `@Transactional` on the service layer, keeping entities managed during the decision phase.
    *   Convert mutable JPA Entities to immutable domain records *before* leaving the transaction boundary to prevent leaking unmanaged states.
    *   Implement **Pessimistic Locking** (e.g., locking the `Member` row) to serialize concurrent requests checking conditions for the same resource.
*   **Key Files to Study:**
    *   [`PESSIMISTIC-LOCKING.md`](file:///mydata/codes/2026/java21-workout/PESSIMISTIC-LOCKING.md) — Under-the-hood explanation of the "Fitting Room" lock.
    *   [`LOCKING-EXERCISES.md`](file:///mydata/codes/2026/java21-workout/LOCKING-EXERCISES.md) — Interactive locking scenarios with answers.

---

### 📍 Milestone 5: Advanced Java 21+ Concurrency (Virtual Threads & Structured Concurrency)
Scale and optimize execution paths cleanly when scaling beyond sequential dependencies.

*   **The Idea:**
    *   Use `@RunOnVirtualThread` to execute blocking HTTP request handlers on lightweight Virtual Threads rather than blocking expensive carrier/platform threads.
    *   Use `StructuredTaskScope` (with `ShutdownOnFailure` or `ShutdownOnSuccess`) to coordinate parallel non-blocking/IO operations concurrently (e.g., retrieving independent external metadata or logs), ensuring nested task lifetimes are strictly bound and canceled correctly if errors occur.
*   **Key Files to Study:**
    *   [`FUNCTIONAL-MINDSET.md#L70-L89`](file:///mydata/codes/2026/java21-workout/FUNCTIONAL-MINDSET.md#L70-L89) — Virtual Threads and Structured Concurrency orchestration.
    *   [`.kiro/steering/java21-modern.md#L31-L40`](file:///mydata/codes/2026/java21-workout/.kiro/steering/java21-modern.md#L31-L40) — Steering rules on modern concurrency constraints.

---

## 🏃 Active Practice Workout

Ready to put this roadmap into action? Implement the complete **"Return a Book"** feature following these exact concepts end-to-end:

👉 **[exercises/01-return-book.md](file:///mydata/codes/2026/java21-workout/exercises/01-return-book.md)**
