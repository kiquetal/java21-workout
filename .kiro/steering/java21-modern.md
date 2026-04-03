# Java 21+ Modern Steering

## Immutability First
- `record` for all data carriers — no mutable POJOs
- `List.of()`, `Map.of()`, `List.copyOf()` over mutable collections
- No setters — construct once, use everywhere

## Sealed Types as Control Flow
- Model outcomes with `sealed interface` + `record` variants
- Replace exceptions for expected failures with result types
- No `null` returns — use `Optional` or sealed types

## Pattern Matching Everywhere
- `switch` expressions over `if-else` chains
- Deconstruct records directly in `case` clauses
- Use `when` guards instead of nested `if` inside cases
- Exhaustive switches on sealed types — no `default` needed

## Functional Pipelines
- Streams over imperative loops
- `Optional` chaining (`map`/`flatMap`/`orElseThrow`) over null checks
- Method references over lambdas when possible
- Small pure functions composed together

## Type-Driven Design
- Types encode business rules — make illegal states unrepresentable
- `sealed interface` = "one of these" (sum type)
- `record` = "all of these together" (product type)
- Combine both for expressive domain models
