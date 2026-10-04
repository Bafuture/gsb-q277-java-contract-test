package com.example.contract.sample;

/** Another contract-compliant implementation with identical observable behavior. */
public final class StripNormalizer implements TextNormalizer {

    @Override
    public String normalize(String input) {
        return input.strip();
    }
}
