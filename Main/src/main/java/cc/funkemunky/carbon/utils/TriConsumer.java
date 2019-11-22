package cc.funkemunky.carbon.utils;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

    void accept(A one, B two, C three);

    default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
        Objects.requireNonNull(after);

        return (A, B, C) -> {
            accept(A, B, C);
            after.accept(A, B, C);
        };
    }
}
