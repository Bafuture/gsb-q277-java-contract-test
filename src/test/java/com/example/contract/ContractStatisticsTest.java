package com.example.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.contract.sample.LegacyTextProcessor;
import com.example.contract.sample.ModernTextProcessor;
import com.example.contract.sample.StandardTextProcessor;
import com.example.contract.sample.TextProcessorContracts;
import org.junit.jupiter.api.Test;

/** 需求 7：统计契约数、实现数、执行组合数与不满足项。 */
class ContractStatisticsTest {

    @Test
    void statisticsAggregateContractsImplementationsExecutionsAndNonConforming() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor())
                .registerImplementation("legacy", new LegacyTextProcessor())
                .registerImplementation("modern", new ModernTextProcessor());
        suite.verifyAll();

        ContractStatistics stats = suite.statistics();
        assertThat(stats.contractCount()).isEqualTo(1);
        assertThat(stats.implementationCount()).isEqualTo(3);
        assertThat(stats.executionCount()).isEqualTo(3);
        // legacy 有 2 个失败用例，其余组合全部通过。
        assertThat(stats.failedCaseCount()).isEqualTo(2);
        assertThat(stats.nonConformingCount()).isEqualTo(2);
        assertThat(stats.render()).contains("契约数").contains("不满足项");
    }

    @Test
    void missingAndStaleRunsCountAsNonConforming() {
        ContractSuite suite = new ContractSuite()
                .registerContract(TextProcessorContracts.v1())
                .registerImplementation("standard", new StandardTextProcessor());
        suite.verifyAll();

        // 新实现未执行 -> 漏测 +1。
        suite.registerImplementation("modern", new ModernTextProcessor());
        ContractStatistics withMissing = suite.statistics();
        assertThat(withMissing.missingRunCount()).isEqualTo(1);
        assertThat(withMissing.nonConformingCount()).isEqualTo(1);

        // 契约升级 -> 旧记录版本过期 +1（modern 依旧漏测）。
        suite.registerContract(TextProcessorContracts.v2());
        ContractStatistics withStale = suite.statistics();
        assertThat(withStale.staleRunCount()).isEqualTo(1);
        assertThat(withStale.missingRunCount()).isEqualTo(1);
        assertThat(withStale.nonConformingCount()).isEqualTo(2);
    }
}
