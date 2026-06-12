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

## Decisions Made
- `Either` lives in `dev.learning.domain.type` — it's a foundational type, not domain-specific
- Left = sealed result variants (LendingResult, BookItemResult, etc.)
- Right = entities or domain types being passed through the pipeline
- Either is **internal** to service methods (private). Public API returns the sealed result directly.
- `fold` at the end converts Either back to the public result type
