package com.example.contract;

import java.util.List;

/** Shared contract definition used across the tests. */
final class Contracts {

    static final String NORMALIZER_CONTRACT = "text-normalizer";
    static final String TRIMS_WHITESPACE = "trims-whitespace";
    static final String KEEPS_EMPTY_STRING = "keeps-empty-string";
    static final String NULL_INPUT_THROWS_NPE = "null-input-throws-npe";

    private Contracts() {
    }

    static Contract<String, String> normalizerContract(int version) {
        return new Contract<>(NORMALIZER_CONTRACT, version, List.of(
                ContractCase.<String, String>expectingOutput(TRIMS_WHITESPACE, "  hello  ", "hello"),
                ContractCase.<String, String>expectingOutput(KEEPS_EMPTY_STRING, "", ""),
                ContractCase.<String, String>expectingException(NULL_INPUT_THROWS_NPE, null,
                        NullPointerException.class)));
    }
}
