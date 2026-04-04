# Exercise 1: Return a Book

Build the "return a book" feature end-to-end using everything we've practiced.

## What You'll Practice

- Sealed result types (model every outcome)
- Value types (parse, don't validate)
- Pattern matching (exhaustive switch in resource)
- Functional pipelines (Optional chaining with type witnesses)
- Repository pattern (service never touches entities directly)
- DTO → Domain → Entity flow
- Dirty checking (update without persist)

## The Domain

A member returns a book. What can happen?

1. **Success** — lending found, marked as returned
2. **LendingNotFound** — no active lending for this book item + member
3. **AlreadyReturned** — lending exists but was already returned

## Step by Step

### 1. Create the Value Type

File: `domain/type/LendingId.java`

```java
public record LendingId(Long value) {
    public LendingId {
        // validate: not null, positive
    }
}
```

### 2. Create the Command

File: `domain/command/ReturnCommand.java`

Think: what data does the service need to process a return?

### 3. Create the Sealed Result

File: `domain/result/ReturnResult.java`

```java
public sealed interface ReturnResult {
    // define the 3 outcomes
    // each variant carries the data needed for the HTTP response
}
```

### 4. Add Repository Method

File: `repository/LendingRepository.java`

Add a method to find an active lending. Think about the signature:
- What domain type does it accept?
- What does it return? (hint: Optional)

```java
public Optional<BookLending> findActiveLending(LendingId lendingId) {
    // query by id AND status = ACTIVE
}
```

### 5. Write the Service

File: `service/LendingService.java`

Add a `returnBook` method. The logic:

```
find active lending by id
  → not found? → LendingNotFound
  → found but already returned? → AlreadyReturned
  → found and active? → set returnedAt, set status RETURNED → Success
```

Requirements:
- Use `Optional` chaining (no if-else)
- Use `<ReturnResult>map` type witness
- Use dirty checking (no persist call for the update)
- `@Transactional`

### 6. Create the DTOs

File: `dto/ReturnRequest.java` — what the client sends (lending ID)
File: `dto/ReturnResponse.java` — what the client gets back

### 7. Write the Resource

File: `resource/LendingResource.java`

Add a `PUT /api/lendings/{id}/return` endpoint:

```java
@PUT
@Path("/{id}/return")
public Response returnBook(@PathParam("id") Long id) {
    // 1. Parse: Long → LendingId (value type)
    // 2. Build: ReturnCommand
    // 3. Call: lendingService.returnBook(command)
    // 4. Match: switch on ReturnResult → HTTP response
    //    Success → 200 + ReturnResponse
    //    LendingNotFound → 404 + ErrorResponse
    //    AlreadyReturned → 409 + ErrorResponse
}
```

## Constraints

- [ ] Service NEVER calls `entity.persist()` or `Entity.find()`
- [ ] Service receives a command with value types, not primitives
- [ ] Resource does all DTO ↔ Domain parsing
- [ ] Sealed result has no `default` case in switch
- [ ] No exceptions for business failures
- [ ] No null returns anywhere — Optional or sealed types

## Bonus Challenges

### A. Add a Partial Unique Index

Write a Flyway migration that ensures a book item can only have one `ACTIVE` lending at a time.

### B. Add Custom Metrics

Count successful returns and rejected returns using `MeterRegistry`:

```java
registry.counter("lending.return.success").increment();
registry.counter("lending.return.rejected", "reason", "not_found").increment();
```

### C. Add Logging

Use `Logger.getLogger()` to log:
- TRACE: raw request data
- DEBUG: parsed domain types
- INFO: return processed
- WARN: attempted return on already-returned lending

### D. Overdue Check

Before returning, check if `LocalDate.now()` is after `dueDate`. If so, the return still succeeds but the result carries an `overdue = true` flag. Modify the sealed result:

```java
record Success(BookLending lending, boolean overdue) implements ReturnResult {}
```

The resource returns 200 either way, but the response includes `"overdue": true`.

## Verify

When done, run:

```bash
./mvnw compile
```

Then test with curl:

```bash
# Return a lending
curl -X PUT http://localhost:8080/api/lendings/1/return

# Try returning again — should get 409
curl -X PUT http://localhost:8080/api/lendings/1/return

# Non-existent lending — should get 404
curl -X PUT http://localhost:8080/api/lendings/999/return
```

## What You Should Feel After This Exercise

- Building a feature starts with the domain (result type + command), not the endpoint
- The sealed result IS the documentation of what can happen
- The resource is mechanical — parse in, match out
- The service is pure business logic — no HTTP, no DTOs
- Dirty checking means update = mutate the managed entity, no save call
- Value types make it impossible to pass a member ID where a lending ID is expected
