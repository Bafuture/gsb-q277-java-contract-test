package com.example.contract;

import java.util.List;

/**
 * Immutable record of running all cases of one contract (at a specific
 * version) against one implementation.
 */
public final class ContractExecution {

    private final String contractName;
    private final int contractVersion;
    private final String implementationName;
    private final List<CaseResult> results;

    public ContractExecution(String contractName, int contractVersion,
                             String implementationName, List<CaseResult> results) {
        this.contractName = contractName;
        this.contractVersion = contractVersion;
        this.implementationName = implementationName;
        this.results = List.copyOf(results);
    }

    public String contractName() {
        return contractName;
    }

    public int contractVersion() {
        return contractVersion;
    }

    public String implementationName() {
        return implementationName;
    }

    public List<CaseResult> results() {
        return results;
    }

    public long passedCount() {
        return results.stream().filter(CaseResult::isPassed).count();
    }

    public long failedCount() {
        return results.size() - passedCount();
    }

    public List<CaseResult> failures() {
        return results.stream().filter(result -> !result.isPassed()).toList();
    }

    public CaseResult resultFor(String caseId) {
        for (CaseResult result : results) {
            if (result.caseId().equals(caseId)) {
                return result;
            }
        }
        return null;
    }
}
