# Functional Mindset in Java 21 — When and Why

## Core Idea
Model your code as **data transformations** instead of **imperative steps with side effects**.

| Imperative | Functional |
|------------|-----------|
| Throw exceptions for expected failures | Return `Either<Error, Value>` or sealed result |
| Null checks with early returns | `Optional` chains with `map`/`flatMap` |
| Mutable state accumulation | Immutable records flowing through a pipeline |
| `if-else` trees | Pattern matching on sealed types |
| Shared mutable objects | Value types (records) — construct once, never mutate |

## The Three Pillars in This Project

### 1. Sealed Types as Sum Types
"This value is ONE OF these possibilities — and nothing else."

```java
sealed interface LendingResult {
    record Success(LendingDetail book) implements LendingResult {}
    record MemberNotFound(MemberId id) implements LendingResult {}
    record AlreadyLent(LendingDetail detail) implements LendingResult {}
}
```

The compiler enforces exhaustive handling. No forgotten edge cases.

### 2. Records as Product Types
"This value is ALL OF these things together."

```java
record LendCommand(BookItemId bookItemId, MemberId memberId) {}
record LendingDetail(BookItemId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
```

Immutable by construction. Equals/hashCode/toString for free.

### 3. Either + flatMap as Railway Composition
"Chain steps where each can fail. First failure stops the train."

```java
return findMember(cmd)
    .flatMap(this::checkOverdue)
    .flatMap(this::checkMaximumLentNumber)
    .flatMap(m -> findBookItemAndMember(cmd, m))
    .fold(err -> err, this::persistAndReturnResult);
```

See `EITHER-COMPOSITION.md` for deep dive.

## When Functional Style Helps

| Situation | Why functional wins |
|-----------|-------------------|
| Multi-step validation | Each step is independent, composable, testable |
| Multiple error types from one operation | Sealed result > checked exceptions |
| Data transformation (DTO ↔ Domain) | `map` / pattern match — no ceremony |
| Making illegal states unrepresentable | Type system catches bugs at compile time |

## When Functional Style Does NOT Help

| Situation | What to do instead |
|-----------|-------------------|
| Side effects are the point (persist, send email) | Do the imperative thing at the end of the pipeline |
| Simple CRUD with no business rules | Just call the repo and return — no need for Either |
| Performance-critical tight loops | Streams/allocations may matter — measure first |
| Team unfamiliar with the style | Introduce gradually, don't rewrite everything |

## Functional vs Concurrency — Different Axes

```
                    ┌─────────────────────────────┐
                    │       What to compute        │  ← Functional (Either, sealed types)
                    │   (logic, data flow, errors) │
                    └─────────────────────────────┘
                                  ↕  orthogonal
                    ┌─────────────────────────────┐
                    │      How to execute it       │  ← Concurrency (virtual threads, scopes)
                    │   (parallelism, threading)   │
                    └─────────────────────────────┘
```

- **Sequential dependencies** (step N needs step N-1) → functional pipeline, no concurrency needed
- **Independent work** (fetch A, B, C in parallel) → `StructuredTaskScope`, results combined functionally
- **Blocking I/O** → `@RunOnVirtualThread`, code stays the same

They compose — a pipeline can run on a virtual thread, and `StructuredTaskScope` results can feed into `flatMap` chains.

## Pattern: Service Method Shape

Every service method in this project follows:

```
Input (Command) → Pipeline (Either chain) → Output (Sealed Result)
```

1. **Parse**: Resource converts DTO → Command (with validated value types)
2. **Decide**: Service runs the pipeline — pure logic + DB reads via repository
3. **Execute**: Final step in pipeline performs the write (persist/update)
4. **Respond**: Resource pattern-matches the sealed result → HTTP response

Side effects (DB writes) happen only in the last pipeline step. Everything before is a decision.

## Trade-offs Accepted

| Got | Gave up |
|-----|---------|
| Compile-time exhaustiveness | More boilerplate than exceptions |
| Composable error handling | Java's type inference needs help (`Either.<L, R>right(...)`) |
| Self-documenting return types | Team needs to learn the pattern |
| Testable steps in isolation | Slightly more allocations (records per step) |

## Practical Rules

1. **Don't force it** — if a method can't fail, just return the value directly
2. **Either is internal** — services return sealed results, not `Either`
3. **fold() is the exit** — convert Either to the public result type at the end
4. **One pipeline per use case** — don't nest pipelines, compose steps linearly
5. **Side effects last** — reads in the middle, writes at the end
