package com.example.contract;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一份契约对一个实现的完整执行记录（含被验证契约的版本）。
 */
public final class ContractVerification {

    private final String contractName;
    private final int contractVersion;
    private final String implementationName;
    private final List<CaseResult> results;

    public ContractVerification(
            String contractName, int contractVersion, String implementationName, List<CaseResult> results) {
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

    public int totalCount() {
        return results.size();
    }

    public long passedCount() {
        return results.stream().filter(CaseResult::isPassed).count();
    }

    public long failedCount() {
        return results.stream().filter(r -> !r.isPassed()).count();
    }

    public List<CaseResult> failures() {
        return results.stream().filter(r -> !r.isPassed()).collect(Collectors.toList());
    }

    public boolean isConforming() {
        return failedCount() == 0;
    }

    public String summary() {
        StringBuilder builder = new StringBuilder();
        builder.append("contract '").append(contractName).append("' v").append(contractVersion)
                .append(" vs implementation '").append(implementationName).append("': ")
                .append(passedCount()).append('/').append(totalCount()).append(" passed");
        if (!isConforming()) {
            builder.append("; failed cases: ").append(
                    failures().stream().map(CaseResult::caseName).collect(Collectors.joining(", ", "[", "]")));
        }
        return builder.toString();
    }
}
