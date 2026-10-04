package com.example.contract;

/**
 * 单个契约用例：名称 + 输入 + 期望（值或异常）。
 */
public final class ContractCase<I, O> {

    private final String name;
    private final I input;
    private final Expectation<O> expectation;

    public ContractCase(String name, I input, Expectation<O> expectation) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("case name must not be blank");
        }
        if (expectation == null) {
            throw new IllegalArgumentException("expectation must not be null");
        }
        this.name = name;
        this.input = input;
        this.expectation = expectation;
    }

    public static <I, O> ContractCase<I, O> expectingValue(String name, I input, O expected) {
        return new ContractCase<>(name, input, Expectation.value(expected));
    }

    public static <I, O> ContractCase<I, O> expectingException(
            String name, I input, Class<? extends Throwable> exceptionType) {
        return new ContractCase<>(name, input, Expectation.exception(exceptionType));
    }

    public String name() {
        return name;
    }

    public I input() {
        return input;
    }

    public Expectation<O> expectation() {
        return expectation;
    }
}
