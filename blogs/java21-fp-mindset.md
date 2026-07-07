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

Here is a visual representation of how the `Result` container acts as a box holding either a success (`Ok`) or a failure (`Err`) track:

![The Monadic Result Container](file:///mydata/codes/2026/java21-workout/diagrams/result_box.png)

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

- flatMap: We use to compose the results, and the difference with map is that we use a function that returns a Result<T,E> and not create a new Result<T,E>
- map: We use to transform the type of the result. Always boxes the result in a new Result<T,E>
- fold: We use to obtain a type for 2 possible outcomes. In our sceneario is a `LendingResult`
  It is called catamorphism in functional programming.

Here is a visual breakdown of how **map** and **flatMap** behave on our railway tracks:

![Map vs FlatMap Diagram](file:///mydata/codes/2026/java21-workout/diagrams/map_flatmap.png)


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

### 💡 A Key Realization: Identical Decision Trees

If you look closely at the Java implementation of both methods, you'll notice their decision-making tree is **exactly identical**. Both operators pattern-match on `this` to determine the execution path:

```java
return switch (this) {
    case Ok<T, E> ok -> // Execute success path
    case Err<T, E> err -> new Err<>(err.error()); // Bypass on failure
};
```

#### The Only Difference: Who wraps the Result?

Because the decision tree is identical, the only difference between the two operators lies in **who is responsible for returning the final `Result` container**:

1. **`map` (Operator wraps the value)**:
   * Expects a function that returns a raw value `U` (`T -> U`).
   * The operator itself is responsible for boxing that value into a `Result` (`new Ok<>(...)`).
   * *Use case*: When transforming a success value inside the box without returning a new failure.

2. **`flatMap` (Mapper function wraps the value)**:
   * Expects a function that already returns a `Result<U, E>` (`T -> Result<U, E>`).
   * The operator returns the result of the function directly, avoiding double-wrapping (e.g., `Result<Result<U, E>, E>`).
   * *Use case*: When chaining operations that can fail on their own.

By recognizing that both operators share the same core decision tree, the railway model becomes incredibly clean and simple to reason about!

---

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

### Evaluating Every Possibility: The REST Controller Layer

The functional pipeline completes its journey at the API boundary. By utilizing Java 21's powerful **sealed interface pattern matching**, we can evaluate every possible domain outcome of `LendingResult` in a single, clean `switch` expression inside our REST resource/controller:

```java
@POST
public Response register(@Valid LendRequest request) {
    var command = new LendCommand(
        new BookItemId(request.bookId()),
        new MemberId(request.memberId())
    );

    var result = lendingService.lend(command);

    return switch (result) {
        case LendingResult.Success(var detail) -> 
            Response.ok(detail).build();
            
        case LendingResult.AlreadyLent(var detail) -> 
            Response.status(409).entity(new ErrorResponse("Book already lent")).build();
            
        case LendingResult.MemberNotFound(var id) -> 
            Response.status(404).entity(new ErrorResponse("Member not found: " + id.value())).build();
            
        case LendingResult.BookNotFound(var id) -> 
            Response.status(404).entity(new ErrorResponse("Book item not found: " + id)).build();
            
        case LendingResult.MemberHasOverdueBooks(var id, var books) -> 
            Response.status(403).entity(new ErrorResponse("Member has overdue books")).build();
            
        case LendingResult.MaximumLimitReached(var id) -> 
            Response.status(403).entity(new ErrorResponse("Maximum lending limit reached")).build();
    };
}
```

Here is a visual breakdown of how the sealed interface record pattern matching deconstructs options and routes them directly to HTTP statuses:

![Sealed Interface Switch Matching Diagram](file:///mydata/codes/2026/java21-workout/diagrams/sealed_switch.png)

#### Why is this so powerful?

1. **Exhaustiveness Guarantee**: Because `LendingResult` is a sealed hierarchy, the compiler forces us to handle **every single scenario**. If we add a new business rule (e.g., `MemberIsSuspended`), the code will not compile until we explicitly handle that error in our resource layer. No more forgotten exception handlers or untyped internal server errors!
2. **Readability**: The entire business flow is mapped to standard REST responses (`200 OK`, `409 Conflict`, `404 Not Found`, `403 Forbidden`) in a clean, tabular structure that reads like a specification document.
3. **No Side Effects**: There are no hidden exceptions being thrown or caught. Everything is a pure, predictable flow of data from the database query down to the HTTP status code.

---

### Conclusion

By bringing Railway Oriented Programming concepts to Java 21, we can build robust, highly readable, and compile-time safe business pipelines. Combined with sealed interfaces, record patterns, and exhaustive switch expressions, Java has truly evolved into a modern language that supports elegant functional architectures.

If you want to dive deeper, check out the complete implementation in the [GitHub Repository](https://github.com/kiquetal/java21-workout).

