package com.example.contract;

import java.util.function.Function;

/**
 * A named implementation under test. Any implementation class can be
 * adapted via a method reference, e.g.
 * {@code Implementation.of("trimmer", new Trimmer()::normalize)}.
 */
public final class Implementation<I, O> {

    private final String name;
    private final Function<I, O> behavior;

    private Implementation(String name, Function<I, O> behavior) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("implementation name must not be blank");
        }
        if (behavior == null) {
            throw new IllegalArgumentException("implementation behavior must not be null");
        }
        this.name = name;
        this.behavior = behavior;
    }

    public static <I, O> Implementation<I, O> of(String name, Function<I, O> behavior) {
        return new Implementation<>(name, behavior);
    }

    public String name() {
        return name;
    }

    public O invoke(I input) {
        return behavior.apply(input);
    }
}
