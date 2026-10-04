package com.example.contract.sample;

/** 只 trim 不转大写：值用例不满足契约。 */
public final class LegacyTextProcessor implements TextProcessor {
    @Override
    public String process(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input must not be blank");
        }
        return input.trim();
    }
}
