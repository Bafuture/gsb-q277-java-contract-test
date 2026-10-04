package com.example.contract.sample;

/** Contract-compliant implementation: trims, throws NPE on null. */
public final class TrimNormalizer implements TextNormalizer {

    @Override
    public String normalize(String input) {
        return input.trim();
    }
}
