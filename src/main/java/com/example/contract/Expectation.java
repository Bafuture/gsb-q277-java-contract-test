package com.example.contract;

/**
 * 契约用例的期望：要么期望返回一个值，要么期望抛出指定类型的异常。
 */
public final class Expectation<O> {

    public enum Kind {
        VALUE,
        EXCEPTION
    }

    private final Kind kind;
    private final O expectedValue;
    private final Class<? extends Throwable> expectedExceptionType;

    private Expectation(Kind kind, O expectedValue, Class<? extends Throwable> expectedExceptionType) {
        this.kind = kind;
        this.expectedValue = expectedValue;
        this.expectedExceptionType = expectedExceptionType;
    }

    public static <O> Expectation<O> value(O expected) {
        return new Expectation<>(Kind.VALUE, expected, null);
    }

    public static <O> Expectation<O> exception(Class<? extends Throwable> type) {
        if (type == null) {
            throw new IllegalArgumentException("expected exception type must not be null");
        }
        return new Expectation<>(Kind.EXCEPTION, null, type);
    }

    public Kind kind() {
        return kind;
    }

    public boolean isException() {
        return kind == Kind.EXCEPTION;
    }

    public O expectedValue() {
        return expectedValue;
    }

    public Class<? extends Throwable> expectedExceptionType() {
        return expectedExceptionType;
    }

    public String describe() {
        if (isException()) {
            return "exception " + expectedExceptionType.getName();
        }
        return "value <" + expectedValue + ">";
    }
}
