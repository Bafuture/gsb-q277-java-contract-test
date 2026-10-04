package com.example.contract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Raised when implementations verified against the same contract version
 * produce different outcomes (different outputs or different exception
 * types) for the same case.
 */
public final class ConsistencyViolation {

    private final String contractName;
    private final String caseId;
    private final Map<String, String> actualsByImplementation;

    public ConsistencyViolation(String contractName, String caseId,
                                Map<String, String> actualsByImplementation) {
        this.contractName = contractName;
        this.caseId = caseId;
        this.actualsByImplementation = new LinkedHashMap<>(actualsByImplementation);
    }

    public String contractName() {
        return contractName;
    }

    public String caseId() {
        return caseId;
    }

    /** Implementation name -> description of its actual outcome. */
    public Map<String, String> actualsByImplementation() {
        return actualsByImplementation;
    }

    public String describe() {
        String actuals = actualsByImplementation.entrySet().stream()
                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                .collect(Collectors.joining(", "));
        return "contract '" + contractName + "' case '" + caseId + "' inconsistent: " + actuals;
    }
}
