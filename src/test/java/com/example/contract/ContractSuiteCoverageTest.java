package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.LegacyTextProcessor;
import com.example.contract.sample.ModernTextProcessor;
import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 需求 3：实现清单自动发现，注册但未执行的组合报告漏测；需求 5：覆盖报告。 */
class ContractSuiteCoverageTest {

    @Test
    void coverageReportCountsPassedFailedAndNotRun() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("legacy", new LegacyTextProcessor())
                .registerImplementation("modern", new ModernTextProcessor());
        suite.verifyAll();

        CoverageReport report = suite.coverageReport();
        assertThat(report.isFullyCovered()).isFalse();

        CoverageReport.Row standardRow =
                report.rowsForImplementation("standard").get(0);
        assertThat(standardRow.status()).isEqualTo(CoverageReport.Status.VERIFIED);
        assertThat(standardRow.passed()).isEqualTo(3);
        assertThat(standardRow.failed()).isZero();
        assertThat(standardRow.notRun()).isZero();

        CoverageReport.Row legacyRow = report.rowsForImplementation("legacy").get(0);
        assertThat(legacyRow.status()).isEqualTo(CoverageReport.Status.VERIFIED);
        assertThat(legacyRow.failed()).isEqualTo(2);
        assertThat(legacyRow.passed()).isEqualTo(1);
    }

    @Test
    void registeredImplementationWithoutRecordIsReportedAsMissing() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("legacy", new LegacyTextProcessor());
        suite.verifyAll();

        // 之后新登记一个实现，但不再执行：清单自动发现它缺执行记录。
        suite.registerImplementation("modern", new ModernTextProcessor());

        CoverageReport report = suite.coverageReport();
        List<CoverageReport.Row> missing = report.missingRuns();
        assertThat(missing).hasSize(1);
        CoverageReport.Row missingRow = missing.get(0);
        assertThat(missingRow.implementationName()).isEqualTo("modern");
        assertThat(missingRow.status()).isEqualTo(CoverageReport.Status.NOT_EXECUTED);
        assertThat(missingRow.notRun()).isEqualTo(3);
        assertThat(report.render()).contains("modern").contains("漏测");
    }
}
