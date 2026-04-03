---
name: java-records
description: Guide for Java 21+ records — when and how to use them as DTOs, value objects, and Panache projections. Use when the learner asks about records or needs to create a DTO.
---

# Java Records

## What is a Record?
A `record` is an immutable data carrier introduced in Java 16. The compiler generates `equals()`, `hashCode()`, `toString()`, accessors, and constructor.

## When to Use
- DTOs (request/response objects)
- Value objects (Money, Email, DateRange)
- Panache query projections
- Map keys, Set elements (correct equals/hashCode for free)
- Configuration holders

## When NOT to Use
- JPA entities (need no-arg constructor + mutable fields)
- Objects that need inheritance (records are final)
- Objects requiring mutable state

## Patterns

### Basic Record
```java
public record TaskResponse(Long id, String title, String status) {}
```

### Compact Constructor (validation)
```java
public record Email(String value) {
    public Email {
        if (value == null || !value.contains("@"))
            throw new IllegalArgumentException("Invalid email: " + value);
    }
}
```

### Record with Derived Data
```java
public record FullName(String first, String last) {
    public String display() {
        return first + " " + last;
    }
}
```

### Record as Panache Projection
```java
// In repository:
public List<TaskSummary> findSummaries() {
    return find("SELECT t.id, t.title FROM Task t")
        .project(TaskSummary.class)
        .list();
}

public record TaskSummary(Long id, String title) {}
```

### Mapping Entity → Record
```java
public record TaskResponse(Long id, String title, String status) {
    public static TaskResponse from(Task entity) {
        return new TaskResponse(entity.id, entity.title, entity.status.name());
    }
}
```
