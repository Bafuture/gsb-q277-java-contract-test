package com.example.contract;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 跨实现一致性报告：同一份契约下，同一用例在不同实现上的实际行为必须一致
 * （返回值相同，或抛出的异常类型相同）。不一致时输出每个实现的实际异常/返回值。
 */
public final class ConsistencyReport {

    public record Mismatch(String contractName, String caseName, Map<String, String> outcomeByImplementation) {
        public String render() {
            String outcomes = outcomeByImplementation.entrySet().stream()
                    .map(entry -> "    - '" + entry.getKey() + "': " + entry.getValue())
                    .collect(Collectors.joining("\n"));
            return "contract '" + contractName + "' case '" + caseName
                    + "' 在各实现上行为不一致：\n" + outcomes;
        }
    }

    private final String contractName;
    private final List<Mismatch> mismatches;

    public ConsistencyReport(String contractName, List<Mismatch> mismatches) {
        this.contractName = contractName;
        this.mismatches = List.copyOf(mismatches);
    }

    public String contractName() {
        return contractName;
    }

    public List<Mismatch> mismatches() {
        return mismatches;
    }

    public boolean isConsistent() {
        return mismatches.isEmpty();
    }

    public String render() {
        StringBuilder builder = new StringBuilder("== 一致性报告 (contract '")
                .append(contractName).append("') ==\n");
        if (mismatches.isEmpty()) {
            builder.append("所有实现行为一致。\n");
        } else {
            for (Mismatch mismatch : mismatches) {
                builder.append(mismatch.render()).append('\n');
            }
        }
        return builder.toString();
    }

    static Map<String, String> emptyOutcomes() {
        return new LinkedHashMap<>();
    }
}
