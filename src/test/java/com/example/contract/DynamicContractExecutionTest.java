package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.TextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import com.example.contract.support.ContractDynamicTests;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/** 需求 2：契约用例展开为 JUnit 动态测试，每个用例一个可定位的测试节点。 */
class DynamicContractExecutionTest {

    private final Contract<TextProcessor, String, String> contract = TextProcessorContracts.v1();

    @TestFactory
    Stream<DynamicTest> standardImplementationContract() {
        return ContractDynamicTests.forImplementation(contract, "standard", new StandardTextProcessor());
    }

    @Test
    void dynamicTestsAreNamedPerCaseForFailureLocalization() {
        List<DynamicTest> tests = ContractDynamicTests
                .forImplementation(contract, "standard", new StandardTextProcessor())
                .toList();

        assertThat(tests).hasSize(3);
        assertThat(tests.get(0).getDisplayName())
                .contains("text-processor")
                .contains("standard")
                .contains("trims and uppercases");
    }
}
