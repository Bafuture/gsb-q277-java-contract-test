package com.example.contract;

import com.example.contract.sample.IllegalArgumentNormalizer;
import com.example.contract.sample.LenientNullNormalizer;
import com.example.contract.sample.TextNormalizer;
import com.example.contract.sample.TrimNormalizer;
import com.example.contract.sample.UpperCaseNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContractExecutionTest {

    private static Implementation<String, String> impl(TextNormalizer normalizer, String name) {
        return Implementation.of(name, normalizer::normalize);
    }

    @Test
    void runsEveryContractCaseAgainstEveryRegisteredImplementation() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));

        List<ContractExecution> executions = registry.verifyAll();

        assertThat(executions).hasSize(1);
        ContractExecution execution = executions.get(0);
        assertThat(execution.contractName()).isEqualTo(Contracts.NORMALIZER_CONTRACT);
        assertThat(execution.implementationName()).isEqualTo("trim");
        assertThat(execution.results()).hasSize(3);
        assertThat(execution.passedCount()).isEqualTo(3);
        assertThat(execution.failures()).isEmpty();
    }

    @Test
    void failurePinpointsTheOffendingCaseAndShowsTheActualOutput() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new UpperCaseNormalizer(), "uppercase"));

        ContractExecution execution = registry.verify(Contracts.NORMALIZER_CONTRACT, "uppercase");

        assertThat(execution.failures()).hasSize(1);
        CaseResult failure = execution.failures().get(0);
        assertThat(failure.caseId()).isEqualTo(Contracts.TRIMS_WHITESPACE);
        assertThat(failure.describe())
                .contains(Contracts.TRIMS_WHITESPACE)
                .contains("HELLO");
        assertThat(execution.resultFor(Contracts.TRIMS_WHITESPACE).status()).isEqualTo(CaseResult.Status.FAILED);
    }

    @Test
    void wrongExceptionTypeFailsTheContractAndReportsTheActualException() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new IllegalArgumentNormalizer(), "illegal-argument"));

        ContractExecution execution = registry.verify(Contracts.NORMALIZER_CONTRACT, "illegal-argument");

        CaseResult failure = execution.resultFor(Contracts.NULL_INPUT_THROWS_NPE);
        assertThat(failure.status()).isEqualTo(CaseResult.Status.FAILED);
        assertThat(failure.expectation()).contains(NullPointerException.class.getName());
        assertThat(failure.actualExceptionType()).isEqualTo(IllegalArgumentException.class);
        assertThat(failure.actualDescription())
                .contains(IllegalArgumentException.class.getName())
                .contains("input must not be null");
    }

    @Test
    void returningOutputInsteadOfExpectedExceptionFailsTheContract() {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new LenientNullNormalizer(), "lenient"));

        ContractExecution execution = registry.verify(Contracts.NORMALIZER_CONTRACT, "lenient");

        CaseResult failure = execution.resultFor(Contracts.NULL_INPUT_THROWS_NPE);
        assertThat(failure.status()).isEqualTo(CaseResult.Status.FAILED);
        assertThat(failure.actualExceptionType()).isNull();
        assertThat(failure.actualDescription()).contains("output ");
        assertThat(failure.describe()).contains(Contracts.NULL_INPUT_THROWS_NPE);
    }

    @Test
    void unexpectedExceptionOnOutputCaseFailsTheContract() {
        ContractCase<String, String> throwingCase =
                ContractCase.expectingOutput("no-exception-expected", "x", "x");
        Contract<String, String> contract = new Contract<>("simple", 1, List.of(throwingCase));
        Implementation<String, String> alwaysFails =
                Implementation.of("always-fails", input -> {
                    throw new IllegalStateException("boom");
                });

        ContractExecution execution = new ContractRunner().run(contract, alwaysFails);

        CaseResult failure = execution.resultFor("no-exception-expected");
        assertThat(failure.status()).isEqualTo(CaseResult.Status.FAILED);
        assertThat(failure.actualDescription())
                .contains(IllegalStateException.class.getName())
                .contains("boom");
    }
}
