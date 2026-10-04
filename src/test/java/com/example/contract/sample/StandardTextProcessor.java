package com.example.contract.sample;

/** 完全符合契约的实现。 */
public final class StandardTextProcessor implements TextProcessor {
    @Override
    public String process(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input must not be blank");
        }
        return input.trim().toUpperCase();
    }
}
