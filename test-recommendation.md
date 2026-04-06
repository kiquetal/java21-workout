# Test Strategy

## One Test Class Per Concept

No `@Nested`. One file per type, per service, per resource. Small, focused, easy to find.

```
src/test/java/dev/learning/
├── domain/type/       BookIdTest, IsbnTest, EmailTest, MemberIdTest
├── domain/result/     BookItemResultTest, LendingResultTest
├── service/           LendingServiceTest, MemberServiceTest, BookItemServiceTest
├── repository/        LendingRepositoryTest, MemberRepositoryTest
└── resource/          LendingResourceTest, MemberResourceTest, BookItemResourceTest
```

## What to Test Where

| What you're testing | DB? | Mocks? | Annotation |
|---|---|---|---|
| Value types (BookId, Isbn, Email) | No | No | Plain `@Test` |
| Sealed results (pattern matching) | No | No | Plain `@Test` |
| Service logic (decisions) | No | `@InjectMock` repositories | `@QuarkusTest` |
| Repository queries (JPQL/SQL) | Yes (Dev Services) | No | `@QuarkusTest` |
| Resource full flow (JSON → DB → response) | Yes (Dev Services) | No | `@QuarkusTest` |

## Value Types — Pure Domain, No Framework

```java
class BookIdTest {
    @Test void validId() {
        assertThat(new BookId(1L).value()).isEqualTo(1L);
    }

    @Test void rejectsNull() {
        assertThatThrownBy(() -> new BookId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

No Quarkus, no DB, no mocks. Runs in milliseconds.

## Services — Mock Repositories, Test Decisions

```java
@QuarkusTest
class LendingServiceTest {

    @Inject
    LendingService lendingService;

    @InjectMock
    MemberRepository memberRepository;

    @InjectMock
    LendingRepository lendingRepository;

    @Test
    void memberNotFound_returnsNotFound() {
        when(memberRepository.findByMemberIdForUpdate(new MemberId(99L)))
            .thenReturn(Optional.empty());

        var result = lendingService.lend(someCommand(99L));

        assertThat(result).isInstanceOf(LendingResult.MemberNotFound.class);
    }
}
```

The question: "Given this data, does my service make the right decision?"

## Repositories — Real DB, Test Queries

```java
@QuarkusTest
class LendingRepositoryTest {

    @Inject
    LendingRepository lendingRepository;

    @Test
    @Transactional
    void hasOverdueBook_returnsTrueWhenExpired() {
        // insert test data directly
        // ...
        assertThat(lendingRepository.hasOverdueBook(new MemberId(1L))).isTrue();
    }
}
```

The question: "Does my JPQL/SQL actually return the right rows?"

## Resources — Full Integration

```java
@QuarkusTest
class LendingResourceTest {

    @Test
    void validLendRequest_returns200() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 1, "memberId": 1, "dueDate": "2026-06-01"}
                """)
        .when()
            .post("/chiron/api/lendings")
        .then()
            .statusCode(200);
    }
}
```

The question: "Does the full flow work end to end?"

## Rule of Thumb

- Mock when you're testing **decisions**
- Use the DB when you're testing **queries**
- Use both when you're testing **the full flow**
