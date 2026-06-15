package dev.learning.domain.type;

import java.util.function.Function;

public sealed interface Either<L,R> {
    record Left<L, R>(L value) implements Either<L, R>
    {
    }

    record Right<L, R>(R value) implements Either<L, R>
    {
    }

    static <L, R> Either<L, R> left(L value)
    {        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value)
    {
        return new Right<>(value);
    }

    default  <U> Either<L, U> map(Function<R, U> f) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value());
            case Right<L, R> right -> new Right<>(f.apply(right.value()));
        };
    }

    default <U> Either<L,U> flatMap(Function<R,Either<L,U>> f) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value());
            case Right<L, R> right -> f.apply(right.value());
        };
    }

    default <U> U fold(Function<L, U> f, Function<R, U> g) {

        return switch (this) {
            case Left<L, R> left -> f.apply(left.value());
            case Right<L, R> right -> g.apply(right.value());
        };

    }

}
