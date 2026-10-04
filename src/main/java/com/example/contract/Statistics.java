package com.example.contract;

/**
 * Framework-wide statistics: number of contracts, implementations,
 * execution combinations and total unmet items (failed cases, missing or
 * stale verifications, consistency violations).
 */
public final class Statistics {

    private final int contractCount;
    private final int implementationCount;
    private final int executionCount;
    private final int violationCount;

    public Statistics(int contractCount, int implementationCount,
                      int executionCount, int violationCount) {
        this.contractCount = contractCount;
        this.implementationCount = implementationCount;
        this.executionCount = executionCount;
        this.violationCount = violationCount;
    }

    public int contractCount() {
        return contractCount;
    }

    public int implementationCount() {
        return implementationCount;
    }

    public int executionCount() {
        return executionCount;
    }

    public int violationCount() {
        return violationCount;
    }

    @Override
    public String toString() {
        return "Statistics{contracts=" + contractCount
                + ", implementations=" + implementationCount
                + ", executions=" + executionCount
                + ", violations=" + violationCount + '}';
    }
}
