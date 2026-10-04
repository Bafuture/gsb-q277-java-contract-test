package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.ModernTextProcessor;
import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.TextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import org.junit.jupiter.api.Test;

/** 需求 4：契约升级后，未按新版本重新验证的实现被标记为版本过期。 */
class ContractVersionTest {

    @Test
    void implementationsVerifiedAgainstOldVersionAreStaleAfterContractChange() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("modern", new ModernTextProcessor());
        suite.verifyAll();
        assertThat(suite.coverageReport().isFullyCovered()).isTrue();

        // 契约变更：同名契约升级到 v2。
        suite.registerContract(TextProcessorContracts.v2());

        CoverageReport report = suite.coverageReport();
        assertThat(report.staleRuns()).hasSize(2);
        CoverageReport.Row staleRow = report.staleRuns().get(0);
        assertThat(staleRow.verifiedVersion()).isEqualTo(1);
        assertThat(staleRow.contractVersion()).isEqualTo(2);
        assertThat(staleRow.notRun()).isEqualTo(4);
        assertThat(report.render()).contains("版本过期");
        assertThat(suite.statistics().staleRunCount()).isEqualTo(2);
    }

    @Test
    void reVerifyingAgainstNewVersionClearsStaleStatus() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor());
        suite.verifyAll();
        suite.registerContract(TextProcessorContracts.v2());
        assertThat(suite.coverageReport().staleRuns()).hasSize(1);

        suite.verify("text-processor", "standard");

        CoverageReport.Row row = suite.coverageReport().rows().get(0);
        assertThat(row.status()).isEqualTo(CoverageReport.Status.VERIFIED);
        assertThat(row.passed()).isEqualTo(4);
        assertThat(suite.coverageReport().isFullyCovered()).isTrue();
    }
}
