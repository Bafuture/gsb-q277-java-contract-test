package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.ModernTextProcessor;
import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.StrictTextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import org.junit.jupiter.api.Test;

/** 需求 6：同一输入不同实现抛出不同异常类型时，一致性检查应报告并输出实际异常。 */
class ConsistencyReportTest {

    @Test
    void conformingImplementationsAreConsistent() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("modern", new ModernTextProcessor());

        ConsistencyReport report = suite.consistencyReport("text-processor");

        assertThat(report.isConsistent()).isTrue();
        assertThat(report.mismatches()).isEmpty();
    }

    @Test
    void divergingExceptionTypesAreReportedWithActualExceptions() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("strict", new StrictTextProcessor());

        ConsistencyReport report = suite.consistencyReport("text-processor");

        assertThat(report.isConsistent()).isFalse();
        assertThat(report.mismatches()).hasSize(1);
        ConsistencyReport.Mismatch mismatch = report.mismatches().get(0);
        assertThat(mismatch.caseName()).isEqualTo("blank input rejected");
        assertThat(mismatch.outcomeByImplementation().get("standard"))
                .contains(IllegalArgumentException.class.getName());
        assertThat(mismatch.outcomeByImplementation().get("strict"))
                .contains(IllegalStateException.class.getName());
        assertThat(report.render())
                .contains("blank input rejected")
                .contains(IllegalArgumentException.class.getName())
                .contains(IllegalStateException.class.getName());
    }
}
