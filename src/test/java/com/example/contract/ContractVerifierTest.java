package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.LegacyTextProcessor;
import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.TextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import org.junit.jupiter.api.Test;

/** 需求 2：自动跑全部用例，失败可定位到具体用例。 */
class ContractVerifierTest {

    private final Contract<TextProcessor, String, String> contract = TextProcessorContracts.v1();

    @Test
    void conformingImplementationPassesAllCases() {
        ContractVerification verification =
                ContractVerifier.verify(contract, "standard", new StandardTextProcessor());

        assertThat(verification.isConforming()).isTrue();
        assertThat(verification.passedCount()).isEqualTo(3);
        assertThat(verification.failedCount()).isZero();
    }

    @Test
    void sameContractIsReusableAcrossImplementations() {
        ContractVerification standard =
                ContractVerifier.verify(contract, "standard", new StandardTextProcessor());
        ContractVerification legacy =
                ContractVerifier.verify(contract, "legacy", new LegacyTextProcessor());

        assertThat(standard.results()).hasSize(legacy.results().size()).hasSize(3);
        assertThat(standard.isConforming()).isTrue();
        assertThat(legacy.isConforming()).isFalse();
    }

    @Test
    void failingImplementationIsLocatedToTheSpecificCaseWithExpectedAndActual() {
        ContractVerification verification =
                ContractVerifier.verify(contract, "legacy", new LegacyTextProcessor());

        assertThat(verification.failedCount()).isEqualTo(2);
        CaseResult firstFailure = verification.failures().get(0);
        assertThat(firstFailure.caseName()).isEqualTo("trims and uppercases");
        assertThat(firstFailure.failureDetail())
                .contains("trims and uppercases")
                .contains("HELLO")
                .contains("hello");
        assertThat(verification.summary()).contains("legacy").contains("trims and uppercases");
    }
}
