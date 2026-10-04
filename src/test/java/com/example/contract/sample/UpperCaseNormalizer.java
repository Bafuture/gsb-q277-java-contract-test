package com.example.contract.sample;

import java.util.Locale;

/** Violates the output contract by also upper-casing the text. */
public final class UpperCaseNormalizer implements TextNormalizer {

    @Override
    public String normalize(String input) {
        return input.trim().toUpperCase(Locale.ROOT);
    }
}
