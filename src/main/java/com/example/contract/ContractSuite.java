package com.example.contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 契约套件：登记契约与实现清单，执行全部 契约 x 实现 组合并留存记录，
 * 进而产出覆盖报告（含漏测与版本过期）、跨实现一致性报告与统计。
 */
public final class ContractSuite {

    private final Map<String, Contract<?, ?, ?>> contracts = new LinkedHashMap<>();
    private final Map<String, Object> implementations = new LinkedHashMap<>();
    private final Map<String, ContractVerification> records = new LinkedHashMap<>();

    /** 注册（或替换，用于契约版本升级）一份契约。 */
    public ContractSuite registerContract(Contract<?, ?, ?> contract) {
        Objects.requireNonNull(contract, "contract");
        contracts.put(contract.name(), contract);
        return this;
    }

    /** 注册一个被测实现。 */
    public ContractSuite registerImplementation(String name, Object implementation) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("implementation name must not be blank");
        }
        Objects.requireNonNull(implementation, "implementation");
        implementations.put(name, implementation);
        return this;
    }

    public Map<String, Contract<?, ?, ?>> contracts() {
        return Map.copyOf(contracts);
    }

    public Map<String, Object> implementations() {
        return Map.copyOf(implementations);
    }

    /** 对全部 契约 x 实现 组合执行契约并记录结果。 */
    public List<ContractVerification> verifyAll() {
        List<ContractVerification> verifications = new ArrayList<>();
        for (Contract<?, ?, ?> contract : contracts.values()) {
            for (Map.Entry<String, Object> implementation : implementations.entrySet()) {
                verifications.add(verify(contract.name(), implementation.getKey()));
            }
        }
        return verifications;
    }

    /** 对单个 契约 x 实现 组合执行契约并记录结果。 */
    public ContractVerification verify(String contractName, String implementationName) {
        Contract<?, ?, ?> contract = contracts.get(contractName);
        if (contract == null) {
            throw new IllegalArgumentException("unknown contract: " + contractName);
        }
        Object implementation = implementations.get(implementationName);
        if (implementation == null) {
            throw new IllegalArgumentException("unknown implementation: " + implementationName);
        }
        ContractVerification verification = runUnchecked(contract, implementationName, implementation);
        records.put(recordKey(contractName, implementationName), verification);
        return verification;
    }

    /** 已记录的验证结果（key 为 契约名#实现名）。 */
    public Map<String, ContractVerification> records() {
        return Map.copyOf(records);
    }

    /** 覆盖报告：每个组合按当前契约版本给出 通过/失败/未执行 用例数。 */
    public CoverageReport coverageReport() {
        List<CoverageReport.Row> rows = new ArrayList<>();
        for (Contract<?, ?, ?> contract : contracts.values()) {
            for (String implementationName : implementations.keySet()) {
                ContractVerification record = records.get(recordKey(contract.name(), implementationName));
                int caseCount = contract.cases().size();
                if (record == null) {
                    rows.add(new CoverageReport.Row(
                            contract.name(), contract.version(), null, implementationName,
                            CoverageReport.Status.NOT_EXECUTED, 0, 0, caseCount));
                } else if (record.contractVersion() != contract.version()) {
                    rows.add(new CoverageReport.Row(
                            contract.name(), contract.version(), record.contractVersion(),
                            implementationName, CoverageReport.Status.STALE_VERSION, 0, 0, caseCount));
                } else {
                    rows.add(new CoverageReport.Row(
                            contract.name(), contract.version(), record.contractVersion(),
                            implementationName, CoverageReport.Status.VERIFIED,
                            (int) record.passedCount(), (int) record.failedCount(), 0));
                }
            }
        }
        return new CoverageReport(rows);
    }

    /**
     * 跨实现一致性：对指定契约在所有已注册实现上即时执行，
     * 同一用例的实际行为（返回值或异常类型）必须一致。
     */
    public ConsistencyReport consistencyReport(String contractName) {
        Contract<?, ?, ?> contract = contracts.get(contractName);
        if (contract == null) {
            throw new IllegalArgumentException("unknown contract: " + contractName);
        }
        Map<String, ContractVerification> perImpl = new LinkedHashMap<>();
        for (Map.Entry<String, Object> implementation : implementations.entrySet()) {
            perImpl.put(implementation.getKey(),
                    runUnchecked(contract, implementation.getKey(), implementation.getValue()));
        }
        List<ConsistencyReport.Mismatch> mismatches = new ArrayList<>();
        for (int caseIndex = 0; caseIndex < contract.cases().size(); caseIndex++) {
            Map<String, String> outcomes = new LinkedHashMap<>();
            for (Map.Entry<String, ContractVerification> entry : perImpl.entrySet()) {
                CaseResult result = entry.getValue().results().get(caseIndex);
                outcomes.put(entry.getKey(), result.outcomeSignature());
            }
            if (outcomes.values().stream().distinct().count() > 1) {
                mismatches.add(new ConsistencyReport.Mismatch(
                        contractName, contract.cases().get(caseIndex).name(), outcomes));
            }
        }
        return new ConsistencyReport(contractName, mismatches);
    }

    /** 汇总统计。 */
    public ContractStatistics statistics() {
        CoverageReport coverage = coverageReport();
        int failedCases = coverage.rows().stream().mapToInt(CoverageReport.Row::failed).sum();
        return new ContractStatistics(
                contracts.size(),
                implementations.size(),
                records.size(),
                failedCases,
                coverage.missingRuns().size(),
                coverage.staleRuns().size());
    }

    private static String recordKey(String contractName, String implementationName) {
        return contractName + "#" + implementationName;
    }

    @SuppressWarnings("unchecked")
    private static <T, I, O> ContractVerification runUnchecked(
            Contract<T, I, O> contract, String implementationName, Object implementation) {
        return ContractVerifier.verify(contract, implementationName, (T) implementation);
    }
}
