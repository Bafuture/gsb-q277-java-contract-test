package com.example.contract.sample;

/** 空白输入抛 IllegalStateException 而非契约要求的 IllegalArgumentException。 */
public final class StrictTextProcessor implements TextProcessor {
    @Override
    public String process(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalStateException("blank input rejected");
        }
        return input.trim().toUpperCase();
    }
}
