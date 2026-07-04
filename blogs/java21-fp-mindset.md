#### Blog to be posted


I have been doing a lot of programming in F# and wanted to try to replicate the mindset in java21.

The Railway-Pattern

We can model the Result<T,E> in F#, which consist in boxing 2 possible scenario the OK and ERR

In f# we can model the following:

```f#
type Result<'T,'E> =
 | Ok of 'T
 | Err of 'E
```

The Result is the key to the Railway-Pattern, which is a way to model the computation.

We can create this in java21 with the following:

```java
sealed interface Result<T, E> {

    record Ok<T, E>(T value) implements Result<T, E> {}
    record Err<T, E>(E error) implements Result<T, E> {}
}
```
Here we can see the result as a sealed interface, this give us the order to always evaluate the two cases.

In addition to Railway-Pattern. we need to create a context for specific use case, we will use the example of a LendingService for a bookstore.

If we want to isolate the possbile outcomes of this specific use case,we could create a set of Result for each use case.

In java21 we can model this with the following:

```java
interface LendingResult {
    record Success(BookLending lending) implements LendingResult {}
    record MemberNotFound(MemberId memberId) implements LendingResult {}
    record MemberHasOverdueBooks(MemberId memberId) implements LendingResult {}
    record BookItemNotAvailable(BookItemId bookItemId) implements LendingResult {}
}
```
Here we ensure all the possible outcomes of the LendingService,
we then need a way to compose the callers, for example the first step to validate the user, then find the book and finally the lending.

For that reason we need to use 3 operators

- flatMap
- map
- fold

We implement flatmap, map, and fold in java21 with the following:

```java
sealed interface Result<T, E> {

    record Ok<T, E>(T value) implements Result<T, E> {}
    record Err<T, E>(E error) implements Result<T, E> {}

    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        return switch (this) {
            case Ok<T, E> ok -> mapper.apply(ok.value());
            case Err<T, E> err -> new Err<>(err.error());
        };
    }

    default <U> Result<U, E> map(Function<T, U> mapper) {
        return switch (this) {
            case Ok<T, E> ok -> new Ok<>(mapper.apply(ok.value()));
            case Err<T, E> err -> new Err<>(err.error());
        };
    }

    default <U> U fold(Function<T, U> onSuccess, Function<E, U> onFailure) {
        return switch (this) {
            case Ok<T, E> ok -> onSuccess.apply(ok.value());
            case Err<T, E> err -> onFailure.apply(err.error());
        };
    }
}
```
The most important part here is the signature, we need to deal with the *right* side of the result and then apply the function which returns a new type.

### Wait, Why Not Use Optional?

A common question is: *"Why not just use Java's built-in `Optional`?"*

While `Optional` is great for representing the *absence* of a value, it has a fatal flaw for business pipelines: **it cannot carry an error payload**. If an operation fails, `Optional.empty()` won't tell you *why* (e.g., whether the member was not found, or if they had overdue books). `Result<T, E>` keeps errors as first-class citizens, preserving rich domain-specific error payloads.

---

### Visualizing the Railway Flow

Here is how the request flows through our pipeline. You can see how each step transitions forward on success or branches down to its specific `LendingResult` outcome (our custom-tailored errors) on failure:

![Lending Flow Diagram](file:///mydata/codes/2026/java21-workout/diagrams/lending_flow_handwritten.png)

---

This is how we can compose multiple steps in the `LendingService` to form a complete railway:

```java
@Transactional
public LendingResult lend(LendCommand lendCommand) {
    return findMember(lendCommand)
            .flatMap(this::checkOverdue)
            .flatMap(this::checkMaximumLentNumber)
            .flatMap(m -> findBookItemAndMember(lendCommand, m))
            .flatMap(this::checkIfAlreadyLent)
            .fold(err -> err, this::persistAndReturnResult);
}
```

Here, each step returns a `Result` (or `Either`). If any step fails, the computation immediately switches to the "error track" (Left / Err), bypassing all subsequent steps. If all steps succeed, it executes the final success track (Right / Ok), persisting the lending and returning the success outcome.
