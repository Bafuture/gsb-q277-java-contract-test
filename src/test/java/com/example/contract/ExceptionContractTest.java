package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.StrictTextProcessor;
import com.example.contract.sample.TextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import org.junit.jupiter.api.Test;

/** 需求 6：异常契约；不同异常类型视为不满足，并输出实际异常。 */
class ExceptionContractTest {

    private final Contract<TextProcessor, String, String> contract = TextProcessorContracts.v1();

    @Test
    void expectedExceptionTypePasses() {
        ContractVerification verification =
                ContractVerifier.verify(contract, "standard", new StandardTextProcessor());
        assertThat(verification.isConforming()).isTrue();
    }

    @Test
    void differentExceptionTypeFailsAndReportsActualException() {
        ContractVerification verification =
                ContractVerifier.verify(contract, "strict", new StrictTextProcessor());

        assertThat(verification.isConforming()).isFalse();
        CaseResult failure = verification.failures().get(0);
        assertThat(failure.caseName()).isEqualTo("blank input rejected");
        assertThat(failure.actualExceptionType()).isEqualTo(IllegalStateException.class);
        assertThat(failure.failureDetail())
                .contains(IllegalArgumentException.class.getName())
                .contains(IllegalStateException.class.getName())
                .contains("blank input rejected");
    }

    @Test
    void returningValueWhenExceptionExpectedFails() {
        Contract<TextProcessor, String, String> contract =
                Contract.<TextProcessor, String, String>define("no-blank-check", TextProcessor::process)
                        .caseException("blank should be rejected", " ", IllegalArgumentException.class)
                        .build();

        TextProcessor neverThrows = input -> input.trim().toUpperCase();
        ContractVerification verification =
                ContractVerifier.verify(contract, "never-throws", neverThrows);

        assertThat(verification.isConforming()).isFalse();
        assertThat(verification.failures().get(0).failureDetail()).contains("expected");
    }
}
