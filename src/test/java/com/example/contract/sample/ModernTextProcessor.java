package com.example.contract.sample;

/** 另一个完全符合契约的实现（用于一致性正例）。 */
public final class ModernTextProcessor implements TextProcessor {
    @Override
    public String process(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("blank input");
        }
        return input.strip().toUpperCase(java.util.Locale.ROOT);
    }
}
