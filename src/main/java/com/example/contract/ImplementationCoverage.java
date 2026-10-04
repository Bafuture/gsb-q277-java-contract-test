package com.example.contract;

import java.util.List;

/**
 * Per-implementation coverage summary: how many cases passed, failed or
 * were never executed against the current contract version.
 */
public final class ImplementationCoverage {

    private final String contractName;
    private final String implementationName;
    private final int passed;
    private final int failed;
    private final int unexecuted;
    private final boolean executed;
    private final boolean stale;
    private final Integer executedVersion;
    private final List<String> failedCaseIds;

    private ImplementationCoverage(String contractName, String implementationName,
                                   int passed, int failed, int unexecuted,
                                   boolean executed, boolean stale, Integer executedVersion,
                                   List<String> failedCaseIds) {
        this.contractName = contractName;
        this.implementationName = implementationName;
        this.passed = passed;
        this.failed = failed;
        this.unexecuted = unexecuted;
        this.executed = executed;
        this.stale = stale;
        this.executedVersion = executedVersion;
        this.failedCaseIds = List.copyOf(failedCaseIds);
    }

    /** The implementation was registered but never verified. */
    public static ImplementationCoverage missing(String contractName, String implementationName, int totalCases) {
        return new ImplementationCoverage(contractName, implementationName, 0, 0, totalCases,
                false, false, null, List.of());
    }

    /** The implementation was verified, but against an older contract version. */
    public static ImplementationCoverage stale(String contractName, String implementationName,
                                               int totalCases, int executedVersion) {
        return new ImplementationCoverage(contractName, implementationName, 0, 0, totalCases,
                true, true, executedVersion, List.of());
    }

    public static ImplementationCoverage verified(String contractName, String implementationName,
                                                  int passed, int failed, int unexecuted,
                                                  int executedVersion, List<String> failedCaseIds) {
        return new ImplementationCoverage(contractName, implementationName, passed, failed, unexecuted,
                true, false, executedVersion, failedCaseIds);
    }

    public String contractName() {
        return contractName;
    }

    public String implementationName() {
        return implementationName;
    }

    public int passed() {
        return passed;
    }

    public int failed() {
        return failed;
    }

    public int unexecuted() {
        return unexecuted;
    }

    /** True when the implementation has no execution record at all. */
    public boolean missing() {
        return !executed;
    }

    /** True when the only execution record targets an older contract version. */
    public boolean stale() {
        return stale;
    }

    public Integer executedVersion() {
        return executedVersion;
    }

    public List<String> failedCaseIds() {
        return failedCaseIds;
    }
}
