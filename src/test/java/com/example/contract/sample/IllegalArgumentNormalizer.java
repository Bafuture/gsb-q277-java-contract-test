package com.example.contract.sample;

/** Violates the exception contract by throwing the wrong exception type on null. */
public final class IllegalArgumentNormalizer implements TextNormalizer {

    @Override
    public String normalize(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        return input.trim();
    }
}
