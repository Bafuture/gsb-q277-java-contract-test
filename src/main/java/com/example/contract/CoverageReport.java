package com.example.contract;

import java.util.List;

/**
 * Aggregated view of the framework state: per-implementation coverage,
 * cross-implementation consistency violations and overall statistics.
 */
public final class CoverageReport {

    private final List<ImplementationCoverage> coverages;
    private final List<ConsistencyViolation> consistencyViolations;
    private final Statistics statistics;

    public CoverageReport(List<ImplementationCoverage> coverages,
                          List<ConsistencyViolation> consistencyViolations,
                          Statistics statistics) {
        this.coverages = List.copyOf(coverages);
        this.consistencyViolations = List.copyOf(consistencyViolations);
        this.statistics = statistics;
    }

    public List<ImplementationCoverage> coverages() {
        return coverages;
    }

    public ImplementationCoverage coverageFor(String contractName, String implementationName) {
        return coverages.stream()
                .filter(coverage -> coverage.contractName().equals(contractName)
                        && coverage.implementationName().equals(implementationName))
                .findFirst()
                .orElse(null);
    }

    /** Registered implementations with no execution record at all. */
    public List<ImplementationCoverage> missingImplementations() {
        return coverages.stream().filter(ImplementationCoverage::missing).toList();
    }

    /** Implementations verified only against an older contract version. */
    public List<ImplementationCoverage> staleImplementations() {
        return coverages.stream().filter(ImplementationCoverage::stale).toList();
    }

    public List<ConsistencyViolation> consistencyViolations() {
        return consistencyViolations;
    }

    public Statistics statistics() {
        return statistics;
    }

    public boolean hasViolations() {
        return statistics.violationCount() > 0;
    }

    /** Human-readable multi-line summary of the report. */
    public String summary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Contract coverage report ===\n");
        for (ImplementationCoverage coverage : coverages) {
            summary.append('[').append(coverage.contractName()).append(" / ")
                    .append(coverage.implementationName()).append("] ");
            if (coverage.missing()) {
                summary.append("MISSING: no execution record, ")
                        .append(coverage.unexecuted()).append(" case(s) unexecuted");
            } else if (coverage.stale()) {
                summary.append("STALE: verified against contract version ")
                        .append(coverage.executedVersion()).append(", ")
                        .append(coverage.unexecuted()).append(" case(s) unexecuted on current version");
            } else {
                summary.append("passed=").append(coverage.passed())
                        .append(" failed=").append(coverage.failed())
                        .append(" unexecuted=").append(coverage.unexecuted());
                if (!coverage.failedCaseIds().isEmpty()) {
                    summary.append(" failedCases=").append(coverage.failedCaseIds());
                }
            }
            summary.append('\n');
        }
        for (ConsistencyViolation violation : consistencyViolations) {
            summary.append("INCONSISTENT: ").append(violation.describe()).append('\n');
        }
        summary.append(statistics);
        return summary.toString();
    }
}
