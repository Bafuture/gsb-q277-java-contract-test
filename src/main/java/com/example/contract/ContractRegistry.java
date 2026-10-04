package com.example.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry tying everything together: contracts and their
 * implementations are registered here, verifications are executed and
 * recorded here, and coverage / consistency reports are built from the
 * recorded executions.
 */
public final class ContractRegistry {

    private static final class Entry<I, O> {
        private Contract<I, O> contract;
        private final List<Implementation<I, O>> implementations = new ArrayList<>();

        private Entry(Contract<I, O> contract) {
            this.contract = contract;
        }
    }

    private final Map<String, Entry<?, ?>> entries = new LinkedHashMap<>();
    private final List<ContractExecution> executions = new ArrayList<>();
    private final ContractRunner runner = new ContractRunner();

    /**
     * Registers a contract, or replaces its definition (e.g. a version
     * bump) while keeping the already registered implementations.
     */
    @SuppressWarnings("unchecked")
    public <I, O> void registerContract(Contract<I, O> contract) {
        Entry<I, O> entry = (Entry<I, O>) entries.get(contract.name());
        if (entry == null) {
            entries.put(contract.name(), new Entry<>(contract));
        } else {
            entry.contract = contract;
        }
    }

    @SuppressWarnings("unchecked")
    public <I, O> void registerImplementation(String contractName, Implementation<I, O> implementation) {
        Entry<I, O> entry = (Entry<I, O>) entries.get(contractName);
        if (entry == null) {
            throw new IllegalArgumentException("unknown contract: " + contractName);
        }
        entry.implementations.add(implementation);
    }

    /** Runs all cases of the contract against the named implementation and records the result. */
    @SuppressWarnings("unchecked")
    public <I, O> ContractExecution verify(String contractName, String implementationName) {
        Entry<I, O> entry = (Entry<I, O>) entries.get(contractName);
        if (entry == null) {
            throw new IllegalArgumentException("unknown contract: " + contractName);
        }
        Implementation<I, O> implementation = entry.implementations.stream()
                .filter(candidate -> candidate.name().equals(implementationName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown implementation '" + implementationName + "' for contract '" + contractName + "'"));
        ContractExecution execution = runner.run(entry.contract, implementation);
        executions.removeIf(record -> record.contractName().equals(contractName)
                && record.implementationName().equals(implementationName));
        executions.add(execution);
        return execution;
    }

    /** Verifies every registered implementation against its contract. */
    public List<ContractExecution> verifyAll() {
        List<ContractExecution> all = new ArrayList<>();
        for (Entry<?, ?> entry : entries.values()) {
            all.addAll(verifyEntry(entry));
        }
        return all;
    }

    private <I, O> List<ContractExecution> verifyEntry(Entry<I, O> entry) {
        List<ContractExecution> all = new ArrayList<>();
        String contractName = entry.contract.name();
        for (Implementation<I, O> implementation : entry.implementations) {
            all.add(verify(contractName, implementation.name()));
        }
        return all;
    }

    public List<ContractExecution> executions() {
        return List.copyOf(executions);
    }

    /** Builds a coverage report from the current registrations and execution records. */
    public CoverageReport report() {
        List<ImplementationCoverage> coverages = new ArrayList<>();
        int violationCount = 0;
        int implementationCount = 0;

        for (Entry<?, ?> entry : entries.values()) {
            Contract<?, ?> contract = entry.contract;
            for (Implementation<?, ?> implementation : entry.implementations) {
                implementationCount++;
                ContractExecution execution = findExecution(contract.name(), implementation.name());
                if (execution == null) {
                    coverages.add(ImplementationCoverage.missing(
                            contract.name(), implementation.name(), contract.cases().size()));
                    violationCount++;
                } else if (execution.contractVersion() != contract.version()) {
                    coverages.add(ImplementationCoverage.stale(
                            contract.name(), implementation.name(), contract.cases().size(),
                            execution.contractVersion()));
                    violationCount++;
                } else {
                    int passed = (int) execution.passedCount();
                    int failed = (int) execution.failedCount();
                    int unexecuted = Math.max(0, contract.cases().size() - execution.results().size());
                    List<String> failedCaseIds = execution.failures().stream()
                            .map(CaseResult::caseId)
                            .toList();
                    coverages.add(ImplementationCoverage.verified(
                            contract.name(), implementation.name(), passed, failed, unexecuted,
                            execution.contractVersion(), failedCaseIds));
                    violationCount += failed + unexecuted;
                }
            }
        }

        List<ConsistencyViolation> consistencyViolations = checkConsistency();
        violationCount += consistencyViolations.size();

        Statistics statistics = new Statistics(entries.size(), implementationCount,
                executions.size(), violationCount);
        return new CoverageReport(coverages, consistencyViolations, statistics);
    }

    /**
     * Compares the outcomes of all implementations verified against the
     * current version of each contract. Two implementations disagreeing on
     * the outcome of the same case (different outputs, or different
     * exception types for the same input) produce a violation.
     */
    public List<ConsistencyViolation> checkConsistency() {
        List<ConsistencyViolation> violations = new ArrayList<>();
        for (Entry<?, ?> entry : entries.values()) {
            Contract<?, ?> contract = entry.contract;
            List<ContractExecution> current = new ArrayList<>();
            for (ContractExecution execution : executions) {
                if (execution.contractName().equals(contract.name())
                        && execution.contractVersion() == contract.version()) {
                    current.add(execution);
                }
            }
            if (current.size() < 2) {
                continue;
            }
            for (ContractCase<?, ?> contractCase : contract.cases()) {
                Map<String, String> actuals = new LinkedHashMap<>();
                String firstKey = null;
                boolean inconsistent = false;
                for (ContractExecution execution : current) {
                    CaseResult result = execution.resultFor(contractCase.id());
                    if (result == null) {
                        continue;
                    }
                    actuals.put(execution.implementationName(), result.actualDescription());
                    if (firstKey == null) {
                        firstKey = result.consistencyKey();
                    } else if (!firstKey.equals(result.consistencyKey())) {
                        inconsistent = true;
                    }
                }
                if (inconsistent) {
                    violations.add(new ConsistencyViolation(contract.name(), contractCase.id(), actuals));
                }
            }
        }
        return violations;
    }

    private ContractExecution findExecution(String contractName, String implementationName) {
        for (ContractExecution execution : executions) {
            if (execution.contractName().equals(contractName)
                    && execution.implementationName().equals(implementationName)) {
                return execution;
            }
        }
        return null;
    }
}
