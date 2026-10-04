package com.example.contract;

import com.example.contract.sample.IllegalArgumentNormalizer;
import com.example.contract.sample.StripNormalizer;
import com.example.contract.sample.TextNormalizer;
import com.example.contract.sample.TrimNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsistencyTest {

    private static Implementation<String, String> impl(TextNormalizer normalizer, String name) {
        return Implementation.of(name, normalizer::normalize);
    }

    @Test
    void implementationsThrowingDifferentExceptionTypesForSameInputAreInconsistent() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new TrimNormalizer(), "trim"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new IllegalArgumentNormalizer(), "illegal-argument"));

        registry.verifyAll();

        List<ConsistencyViolation> violations = registry.checkConsistency();
        assertThat(violations).hasSize(1);
        ConsistencyViolation violation = violations.get(0);
        assertThat(violation.contractName()).isEqualTo(Contracts.NORMALIZER_CONTRACT);
        assertThat(violation.caseId()).isEqualTo(Contracts.NULL_INPUT_THROWS_NPE);
        assertThat(violation.actualsByImplementation())
                .hasEntrySatisfying("trim",
                        actual -> assertThat(actual).contains(NullPointerException.class.getName()))
                .hasEntrySatisfying("illegal-argument",
                        actual -> assertThat(actual).contains(IllegalArgumentException.class.getName()));
        assertThat(violation.describe()).contains("trim").contains("illegal-argument");
    }

    @Test
    void implementationsProducingDifferentOutputsAreInconsistent() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new TrimNormalizer(), "trim"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new com.example.contract.sample.UpperCaseNormalizer(), "uppercase"));

        registry.verifyAll();

        List<ConsistencyViolation> violations = registry.checkConsistency();
        assertThat(violations).extracting(ConsistencyViolation::caseId)
                .containsExactly(Contracts.TRIMS_WHITESPACE);
    }

    @Test
    void implementationsWithEquivalentBehaviorHaveNoConsistencyViolations() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new TrimNormalizer(), "trim"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new StripNormalizer(), "strip"));

        registry.verifyAll();

        assertThat(registry.checkConsistency()).isEmpty();
    }
}
