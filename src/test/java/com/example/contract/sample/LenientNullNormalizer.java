package com.example.contract.sample;

/** Violates the exception contract by tolerating null instead of throwing. */
public final class LenientNullNormalizer implements TextNormalizer {

    @Override
    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}
