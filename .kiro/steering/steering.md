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
├── domain/          # Entities, sealed types, records (core model)
├── resource/        # JAX-RS REST endpoints
├── repository/      # Panache repositories
├── service/         # Business logic
└── dto/             # Request/response records
```

## Naming Conventions
- Records: `XxxRequest`, `XxxResponse`, `XxxDto`
- Sealed interfaces: describe the concept (e.g., `PaymentResult`, `ValidationOutcome`)
- Entities: singular nouns (`Task`, `Project`)
- Resources: `XxxResource`
- Repositories: `XxxRepository`
