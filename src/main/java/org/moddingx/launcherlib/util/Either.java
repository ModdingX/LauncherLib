package org.moddingx.launcherlib.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@link Either} is always either an instance of {@link Either.Left} or {@link Either.Right} effectively
 * giving a union type of two other types.
 */
public interface Either<A, B> {

    /**
     * Gets the left value of this {@link Either} if this is a {@link Left}.
     */
    Optional<A> left();
    
    /**
     * Gets the right value of this {@link Either} if this is a {@link Right}.
     */
    Optional<B> right();

    /**
     * Swaps the order of the elements.
     */
    Either<B, A> swap();

    /**
     * Maps this {@link Either} if it is a {@link Left}.
     */
    <T> Either<T, B> mapLeft(Function<A, T> mapper);
    
    /**
     * Maps this {@link Either} if it is a {@link Right}.
     */
    <T> Either<A, T> mapRight(Function<B, T> mapper);

    /**
     * Maps this {@link Either} with different mapper functions depending on whether this is a {@link Left} or
     * a {@link Right}.
     */
    default <T, U> Either<T, U> map(Function<A, T> leftMapper, Function<B, U> rightMapper) {
        return this.mapLeft(leftMapper).mapRight(rightMapper);
    }

    /**
     * Gets a value from this {@link Either}.
     */
    default <T> T get(Function<A, T> funcA, Function<B, T> funcB) {
        Optional<A> a = this.left();
        if (a.isPresent()) {
            return funcA.apply(a.get());
        } else {
            //noinspection OptionalGetWithoutIsPresent
            return funcB.apply(this.right().get());
        }
    }
    
    /**
     * Gets a value from this {@link Either} if it is a {@link Left} or throws an exception if it is a {@link Right}.
     */
    default <T> T getOrThrow(Function<A, T> funcA, Function<B, ? extends RuntimeException> funcB) {
        Optional<A> a = this.left();
        if (a.isPresent()) {
            return funcA.apply(a.get());
        } else {
            //noinspection OptionalGetWithoutIsPresent
            throw funcB.apply(this.right().get());
        }
    }
    
    /**
     * Gets a value from this {@link Either} if it is a {@link Left} or throws an exception if it is a {@link Right}.
     */
    default <T, E extends Throwable> T getOrThrowChecked(Function<A, T> funcA, Function<B, E> funcB) throws E {
        Optional<A> a = this.left();
        if (a.isPresent()) {
            return funcA.apply(a.get());
        } else {
            //noinspection OptionalGetWithoutIsPresent
            throw funcB.apply(this.right().get());
        }
    }

    /**
     * Creates a new {@link Left} either.
     */
    static <A, B> Either<A, B> left(A a) {
        //noinspection unchecked
        return (Either<A, B>) new Left<A>(a);
    }

    /**
     * Creates a new {@link Right} either.
     */
    static <A, B> Either<A, B> right(B b) {
        //noinspection unchecked
        return (Either<A, B>) new Right<B>(b);
    }

    /**
     * Executes the given {@link Supplier} and returns a {@link Left} with the result. If the supplier throws an
     * exception, returns a {@link Right} containing that exception.
     */
    static <T> Either<T, RuntimeException> tryWith(Supplier<T> action) {
        try {
            return left(action.get());
        } catch (RuntimeException e) {
            return right(e);
        }
    }

    record Left<A>(A value) implements Either<A, Void> {

        @Override
        public Optional<A> left() {
            return Optional.of(this.value());
        }

        @Override
        public Optional<Void> right() {
            return Optional.empty();
        }

        @Override
        public Either<Void, A> swap() {
            return new Right<>(this.value());
        }

        @Override
        public <T> Either<T, Void> mapLeft(Function<A, T> mapper) {
            return new Left<>(mapper.apply(this.value()));
        }

        @Override
        public <T> Either<A, T> mapRight(Function<Void, T> mapper) {
            //noinspection unchecked
            return (Either<A, T>) this;
        }
    }

    record Right<B>(B value) implements Either<Void, B> {

        @Override
        public Optional<Void> left() {
            return Optional.empty();
        }

        @Override
        public Optional<B> right() {
            return Optional.of(this.value());
        }

        @Override
        public Either<B, Void> swap() {
            return new Left<>(this.value());
        }

        @Override
        public <T> Either<T, B> mapLeft(Function<Void, T> mapper) {
            //noinspection unchecked
            return (Either<T, B>) this;
        }

        @Override
        public <T> Either<Void, T> mapRight(Function<B, T> mapper) {
            return new Right<>(mapper.apply(this.value()));
        }
    }
}
