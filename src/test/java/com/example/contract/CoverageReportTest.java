package com.example.contract;

import com.example.contract.sample.TextNormalizer;
import com.example.contract.sample.TrimNormalizer;
import com.example.contract.sample.UpperCaseNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageReportTest {

    private static Implementation<String, String> impl(TextNormalizer normalizer, String name) {
        return Implementation.of(name, normalizer::normalize);
    }

    private static ContractRegistry registryWith(Contract<String, String> contract) {
        ContractRegistry registry = new ContractRegistry();
        registry.registerContract(contract);
        return registry;
    }

    @Test
    void registeredImplementationWithoutExecutionRecordIsReportedAsMissing() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));

        CoverageReport report = registry.report();

        assertThat(report.missingImplementations()).hasSize(1);
        ImplementationCoverage coverage =
                report.coverageFor(Contracts.NORMALIZER_CONTRACT, "trim");
        assertThat(coverage.missing()).isTrue();
        assertThat(coverage.unexecuted()).isEqualTo(3);
        assertThat(report.statistics().violationCount()).isEqualTo(1);
        assertThat(report.summary()).contains("MISSING").contains("trim");
    }

    @Test
    void contractVersionBumpMarksPreviousVerificationsAsStale() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));
        registry.verifyAll();

        registry.registerContract(Contracts.normalizerContract(2));

        CoverageReport report = registry.report();
        assertThat(report.staleImplementations()).hasSize(1);
        ImplementationCoverage coverage =
                report.coverageFor(Contracts.NORMALIZER_CONTRACT, "trim");
        assertThat(coverage.stale()).isTrue();
        assertThat(coverage.executedVersion()).isEqualTo(1);
        assertThat(coverage.unexecuted()).isEqualTo(3);
        assertThat(report.statistics().violationCount()).isEqualTo(1);
        assertThat(report.summary()).contains("STALE");
    }

    @Test
    void reVerificationAgainstNewVersionClearsStaleness() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));
        registry.verifyAll();
        registry.registerContract(Contracts.normalizerContract(2));

        registry.verify(Contracts.NORMALIZER_CONTRACT, "trim");

        CoverageReport report = registry.report();
        assertThat(report.staleImplementations()).isEmpty();
        ImplementationCoverage coverage =
                report.coverageFor(Contracts.NORMALIZER_CONTRACT, "trim");
        assertThat(coverage.executedVersion()).isEqualTo(2);
        assertThat(coverage.passed()).isEqualTo(3);
        assertThat(report.hasViolations()).isFalse();
    }

    @Test
    void coverageCountsPassedFailedAndUnexecutedCasesPerImplementation() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new UpperCaseNormalizer(), "uppercase"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new TrimNormalizer(), "trim"));
        registry.verify(Contracts.NORMALIZER_CONTRACT, "uppercase");

        CoverageReport report = registry.report();

        ImplementationCoverage verified =
                report.coverageFor(Contracts.NORMALIZER_CONTRACT, "uppercase");
        assertThat(verified.passed()).isEqualTo(2);
        assertThat(verified.failed()).isEqualTo(1);
        assertThat(verified.unexecuted()).isZero();
        assertThat(verified.failedCaseIds()).containsExactly(Contracts.TRIMS_WHITESPACE);

        ImplementationCoverage unverified =
                report.coverageFor(Contracts.NORMALIZER_CONTRACT, "trim");
        assertThat(unverified.missing()).isTrue();
        assertThat(unverified.unexecuted()).isEqualTo(3);
    }

    @Test
    void statisticsAggregateContractsImplementationsExecutionsAndViolations() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new UpperCaseNormalizer(), "uppercase"));

        registry.verifyAll();

        Statistics statistics = registry.report().statistics();
        assertThat(statistics.contractCount()).isEqualTo(1);
        assertThat(statistics.implementationCount()).isEqualTo(2);
        assertThat(statistics.executionCount()).isEqualTo(2);
        // one failed case (uppercase trims-whitespace) plus one consistency
        // violation (trim vs uppercase disagree on that case's output)
        assertThat(statistics.violationCount()).isEqualTo(2);
    }

    @Test
    void fullyVerifiedConsistentImplementationsProduceCleanReport() {
        ContractRegistry registry = registryWith(Contracts.normalizerContract(1));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT, impl(new TrimNormalizer(), "trim"));
        registry.registerImplementation(Contracts.NORMALIZER_CONTRACT,
                impl(new com.example.contract.sample.StripNormalizer(), "strip"));

        registry.verifyAll();

        CoverageReport report = registry.report();
        assertThat(report.hasViolations()).isFalse();
        assertThat(report.missingImplementations()).isEmpty();
        assertThat(report.staleImplementations()).isEmpty();
        assertThat(report.consistencyViolations()).isEmpty();
        assertThat(report.coverages())
                .allSatisfy(coverage -> assertThat(coverage.passed()).isEqualTo(3));
    }
}
