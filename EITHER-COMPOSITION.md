# Either<L, R> — Functional Composition in Java 21

## What Is It?
A sealed interface with two slots: Left (error track) and Right (success track).
Same as F#'s `Result<'T, 'E>` but generic — Java doesn't have a built-in one.

```java
public sealed interface Either<L, R> {
    record Left<L, R>(L value) implements Either<L, R> {}
    record Right<L, R>(R value) implements Either<L, R> {}
}
```

## Why Left Only Needs One Value
`new Left<>(error)` — you only provide `L`. The `R` type is a phantom: the compiler infers it from context (the variable type, the return type, the chain). There is no success value — you're on the error track.

Same as F#: `Error "not found"` never mentions the Ok type.

## map vs flatMap

| Method | Function signature | When to use |
|--------|-------------------|-------------|
| `map` | `R -> U` | Step **always succeeds** — just transforms the value |
| `flatMap` | `R -> Either<L, U>` | Step **can fail** — it decides Left or Right |

Key insight: `map` wraps the result in `Right` for you. `flatMap` returns the Either directly (because the function already chose Left or Right).

Both skip execution if `this` is already a Left — the error passes through untouched.

## fold — The Exit Ramp
Takes two functions: one for Left, one for Right. Both return the same type. Collapses the Either into a single value. No more Either after fold.

```java
// Pipeline returns Either<LendingResult, BookLendingResult>
// fold turns it into just LendingResult
return pipeline.fold(
    err -> err,                              // Left already IS a LendingResult
    success -> new LendingResult.Success(success)  // wrap Right into Success
);
```

## Bridging Optional → Either
`Optional` doesn't carry error info. Convert it at the boundary:

```java
return memberOpt
    .map(member -> Either.<LendingResult, Member>right(member))
    .orElse(Either.left(new LendingResult.MemberNotFound(memberId)));
```

## Static Factory Methods (reduce verbosity)
Java's type inference is weaker than F#. Help it with factories:

```java
static <L, R> Either<L, R> left(L value) { return new Left<>(value); }
static <L, R> Either<L, R> right(R value) { return new Right<>(value); }
```

Usage: `Either.left(error)` instead of `new Either.Left<LendingResult, Member>(error)`.
Sometimes you still need the type witness: `Either.<LendingResult, Member>right(member)`.

## The Pipeline Pattern (Railway-Oriented)
Each private method is a step that can fail. Chain them with flatMap:

```java
public LendingResult lend(LendCommand cmd) {
    return findMember(cmd.memberId())          // Either<LendingResult, Member>
        .flatMap(this::checkNoOverdue)         // Either<LendingResult, Member>
        .flatMap(_ -> findBookItem(cmd.bookId()))  // Either<LendingResult, BookItem>
        .flatMap(item -> persistLending(item, cmd)) // Either<LendingResult, BookLendingResult>
        .fold(err -> err, LendingResult.Success::new);
}
```

Each step: if previous was Right, run this step. If Left, skip everything — error flows to fold.

## F# vs Java — Why It Looks Harder Here
F# hides flatMap behind `let!` in computation expressions:
```fsharp
result {
    let! member = findMember memberId    // flatMap hidden by let!
    let! _ = checkOverdue member         // flatMap hidden by let!
    return createLending member
}
```
Java has no computation expressions — you write the `.flatMap(...)` chain manually. Same logic, visible plumbing.

## How The Chain Flows (ASCII)

```
findMember(cmd)          .flatMap(checkNoOverdue)       .flatMap(findBookItem)       .fold(...)
      │                          │                            │                        │
      ▼                          ▼                            ▼                        ▼
┌──────────────┐          ┌──────────────┐             ┌──────────────┐         ┌────────────┐
│ Either<L, R> │──────────│ Either<L, R> │─────────────│ Either<L, R> │────────▶│   plain T  │
└──────────────┘          └──────────────┘             └──────────────┘         └────────────┘
  created here              same box or                  same box or              box opened
                            new one                      new one                  here
```

### What `this` means at each step:

```
findMember(cmd)                      ← creates Either (Left or Right)
       │
       ▼
  [Either A]  ← this inside flatMap IS this object
       │
       │  .flatMap(checkNoOverdue)   ← if Right: calls checkNoOverdue(member), returns new Either
       │                               if Left: skips, returns same Left
       ▼
  [Either B]  ← this inside next flatMap IS this object
       │
       │  .flatMap(findBookItem)
       ▼
  [Either C]  ← this inside fold IS this object
       │
       │  .fold(err -> err, success -> Success(success))
       ▼
  [LendingResult]  ← no more Either, plain value
```

### Left short-circuits everything:

```
findMember returns Left(MemberNotFound)
       │
       ▼
  [Left(MemberNotFound)]
       │
       │  .flatMap(checkNoOverdue)  ← SKIPPED, Left passes through
       ▼
  [Left(MemberNotFound)]
       │
       │  .flatMap(findBookItem)    ← SKIPPED, Left passes through
       ▼
  [Left(MemberNotFound)]
       │
       │  .fold(err -> err, ...)    ← first function called: err -> err
       ▼
  LendingResult.MemberNotFound      ← final answer
```

### Right flows through all steps:

```
findMember returns Right(member)
       │
       ▼
  [Right(member)]
       │
       │  .flatMap(checkNoOverdue)  ← called with member, returns Right(member)
       ▼
  [Right(member)]
       │
       │  .flatMap(findBookItem)    ← called, returns Right(bookItem)
       ▼
  [Right(bookItem)]
       │
       │  .fold(err -> err, success -> Success(success))  ← second function called
       ▼
  LendingResult.Success(bookItem)   ← final answer
```

## Decisions Made
- `Either` lives in `dev.learning.domain.type` — it's a foundational type, not domain-specific
- Left = sealed result variants (LendingResult, BookItemResult, etc.)
- Right = entities or domain types being passed through the pipeline
- Either is **internal** to service methods (private). Public API returns the sealed result directly.
- `fold` at the end converts Either back to the public result type
