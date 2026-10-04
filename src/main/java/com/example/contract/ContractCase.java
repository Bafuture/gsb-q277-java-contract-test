package com.example.contract;

/**
 * A single contract case: one input plus either an expected output or an
 * expected exception type. Cases are reusable across any number of
 * implementations of the contract.
 */
public final class ContractCase<I, O> {

    private final String id;
    private final I input;
    private final O expectedOutput;
    private final Class<? extends Throwable> expectedException;

    private ContractCase(String id, I input, O expectedOutput,
                         Class<? extends Throwable> expectedException) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("case id must not be blank");
        }
        this.id = id;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.expectedException = expectedException;
    }

    public static <I, O> ContractCase<I, O> expectingOutput(String id, I input, O expectedOutput) {
        return new ContractCase<>(id, input, expectedOutput, null);
    }

    public static <I, O> ContractCase<I, O> expectingException(String id, I input,
                                                               Class<? extends Throwable> expectedException) {
        if (expectedException == null) {
            throw new IllegalArgumentException("expected exception type must not be null");
        }
        return new ContractCase<>(id, input, null, expectedException);
    }

    public String id() {
        return id;
    }

    public I input() {
        return input;
    }

    public O expectedOutput() {
        return expectedOutput;
    }

    public Class<? extends Throwable> expectedException() {
        return expectedException;
    }

    public boolean expectsException() {
        return expectedException != null;
    }

    public String expectationDescription() {
        if (expectsException()) {
            return "exception " + expectedException.getName();
        }
        return "output " + String.valueOf(expectedOutput);
    }
}
