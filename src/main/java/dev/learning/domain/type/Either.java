package dev.learning.domain.type;

import java.util.function.Function;

public sealed interface Either<L, R>
{
    record Left<L, R>(L value) implements Either<L, R>
    {
    }

    record Right<L, R>(R value) implements Either<L, R>
    {
    }

    default <U> Either<L,U> map (Function<R,U> f) {

        switch (this) {
            case Left<L, R> left -> {
                return new Left<>(left.value());
            }
            case Right<L, R> right -> {
                return new Right<>(f.apply(right.value()));
            }
        }
    }

    default <U> Either<L,U> flatMap(Function<R,Either<L,U>> f) {

        switch (this) {
            case Left<L, R> left -> {
                return new Left<>(left.value());
            }
            case Right<L, R> right -> {
                return f.apply(right.value());
            }
        }
    }

    default <T> T fold(Function<L,T> left, Function<R,T> right) {

        switch (this) {

            case Left<L, R> l -> {
                return left.apply(l.value());
            }
            case Right<L, R> r -> {
                return right.apply(r.value());
            }
        }



    }

}
