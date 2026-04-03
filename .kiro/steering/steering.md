# Project Steering

## Tech Stack
- Java 21+
- Quarkus (latest)
- Hibernate ORM with Panache
- RESTEasy Reactive
- PostgreSQL (via Dev Services)

## Package Structure
```
src/main/java/dev/learning/
├── domain/          # Core model — never depends on infrastructure
│   ├── entity/      # Panache entities (classes, not records)
│   ├── type/        # Value types — validated records (BookId, Email, Isbn)
│   ├── command/     # Action records (LendCommand, CreateMemberCommand)
│   └── result/      # Sealed result interfaces (LendingResult, CreateMemberResult)
├── repository/      # Panache repositories — all DB access goes here
├── service/         # Business logic — talks to repositories, never to entities directly
├── resource/        # JAX-RS REST endpoints — parsing boundary (DTO ↔ Domain)
└── dto/             # Request/response records — API boundary
```

## Layer Rules
- Services NEVER call static entity methods (`Entity.find`, `Entity.count`, `entity.persist()`)
- Services ALWAYS go through repositories for persistence
- Resources parse DTOs into domain types (commands with value types)
- Resources pattern-match sealed results into HTTP responses
- Repositories accept and return domain types (e.g., `findByMemberId(MemberId)`, not `findById(Long)`)
- Entities are classes (Hibernate requirement), everything else is records

## Naming Conventions
- Records: `XxxRequest`, `XxxResponse`, `XxxDto`
- Sealed interfaces: describe the concept (e.g., `LendingResult`, `CreateMemberResult`)
- Entities: singular nouns (`Book`, `Member`)
- Resources: `XxxResource`
- Repositories: `XxxRepository`
- Commands: `XxxCommand`
- Value types: the concept they represent (`BookId`, `Email`, `Isbn`)
