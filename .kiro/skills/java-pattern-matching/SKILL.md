---
name: java-pattern-matching
description: Guide for Java 21 pattern matching — switch expressions, record deconstruction, guarded patterns. Use when the learner asks about pattern matching or switch expressions.
---

# Pattern Matching in Java 21

## Switch Expressions with Patterns
```java
String describe(Object obj) {
    return switch (obj) {
        case Integer i when i > 0 -> "positive: " + i;
        case Integer i             -> "non-positive: " + i;
        case String s              -> "string: " + s;
        case null                  -> "null";
        default                    -> "other: " + obj;
    };
}
```

## Record Deconstruction
```java
sealed interface Shape {
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}
}

double area(Shape shape) {
    return switch (shape) {
        case Circle(var r)        -> Math.PI * r * r;
        case Rectangle(var w, var h) -> w * h;
    };
}
```

## Guarded Patterns (`when` clause)
```java
String classify(TaskResult result) {
    return switch (result) {
        case Found(var task) when task.priority() == Priority.HIGH -> "urgent";
        case Found(var task)  -> "normal";
        case NotFound(var id) -> "missing: " + id;
        case ValidationError(var errors) -> "invalid: " + errors.size() + " errors";
    };
}
```

## Exhaustiveness
When switching on a sealed type, the compiler ensures all subtypes are covered. No `default` needed — adding a new subtype forces you to handle it everywhere.

## Practical: REST Endpoint with Pattern Matching
```java
@GET
@Path("/{id}")
public Response getTask(@PathParam("id") Long id) {
    return switch (taskService.findById(id)) {
        case Found(var task)              -> Response.ok(task).build();
        case NotFound(var missingId)      -> Response.status(404).build();
        case ValidationError(var errors)  -> Response.status(400).entity(errors).build();
    };
}
```
