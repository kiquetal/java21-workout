---
name: java-sealed-types
description: Guide for sealed interfaces and classes in Java 21+. Use when modeling type hierarchies, result types, or when the learner asks about sealed classes.
---

# Sealed Types in Java

## What Are They?
`sealed` restricts which classes can extend/implement a type. Combined with records, they create algebraic data types in Java.

## Core Pattern: Sealed Interface + Record Implementations
```java
public sealed interface ServiceResult<T> {
    record Success<T>(T data) implements ServiceResult<T> {}
    record NotFound(String message) implements ServiceResult<Object> {}
    record Failure(String error, Exception cause) implements ServiceResult<Object> {}
}
```

## Why Use Sealed Types?
- Compiler enforces exhaustive `switch` — no forgotten cases
- Makes illegal states unrepresentable
- Self-documenting: the type tells you all possible outcomes
- Replaces exception-driven control flow with explicit types

## Patterns

### Result Type (replace exceptions for expected failures)
```java
public sealed interface TaskResult {
    record Found(TaskResponse task) implements TaskResult {}
    record NotFound(Long id) implements TaskResult {}
    record ValidationError(List<String> errors) implements TaskResult {}
}
```

### Command Pattern
```java
public sealed interface TaskCommand {
    record Create(String title, String description) implements TaskCommand {}
    record UpdateStatus(Long id, Status newStatus) implements TaskCommand {}
    record Delete(Long id) implements TaskCommand {}
}
```

### Exhaustive Switch (Java 21)
```java
return switch (result) {
    case TaskResult.Found(var task) -> Response.ok(task).build();
    case TaskResult.NotFound(var id) -> Response.status(404).entity("Task " + id + " not found").build();
    case TaskResult.ValidationError(var errors) -> Response.status(400).entity(errors).build();
};
```
