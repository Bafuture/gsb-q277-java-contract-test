package com.example.contract;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 契约覆盖报告：每个 契约 x 实现 组合的通过 / 失败 / 未执行用例数，
 * 以及漏测（从未执行）和契约版本过期（用旧版本验证过、新版本未验证）状态。
 */
public final class CoverageReport {

    public enum Status {
        /** 已按当前版本契约执行。 */
        VERIFIED,
        /** 注册了实现但没有任何执行记录（漏测）。 */
        NOT_EXECUTED,
        /** 只按旧版本契约验证过，当前新版本未验证。 */
        STALE_VERSION
    }

    public record Row(
            String contractName,
            int contractVersion,
            Integer verifiedVersion,
            String implementationName,
            Status status,
            int passed,
            int failed,
            int notRun) {

        public boolean isConforming() {
            return status == Status.VERIFIED && failed == 0;
        }
    }

    private final List<Row> rows;

    public CoverageReport(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    public List<Row> rows() {
        return rows;
    }

    public List<Row> rowsForImplementation(String implementationName) {
        return rows.stream()
                .filter(row -> row.implementationName().equals(implementationName))
                .collect(Collectors.toList());
    }

    public List<Row> missingRuns() {
        return rows.stream().filter(row -> row.status() == Status.NOT_EXECUTED).collect(Collectors.toList());
    }

    public List<Row> staleRuns() {
        return rows.stream().filter(row -> row.status() == Status.STALE_VERSION).collect(Collectors.toList());
    }

    public boolean isFullyCovered() {
        return rows.stream().allMatch(Row::isConforming);
    }

    public String render() {
        StringBuilder builder = new StringBuilder("== 契约覆盖报告 ==\n");
        for (Row row : rows) {
            builder.append("contract '").append(row.contractName()).append("' v")
                    .append(row.contractVersion())
                    .append(" / implementation '").append(row.implementationName()).append("': ");
            switch (row.status()) {
                case NOT_EXECUTED -> builder.append("未执行（漏测）");
                case STALE_VERSION -> builder.append("版本过期：已按 v")
                        .append(row.verifiedVersion()).append(" 验证，尚未按当前 v")
                        .append(row.contractVersion()).append(" 重新验证");
                case VERIFIED -> builder.append("已验证");
            }
            builder.append("，通过=").append(row.passed())
                    .append(" 失败=").append(row.failed())
                    .append(" 未执行=").append(row.notRun())
                    .append('\n');
        }
        return builder.toString();
    }
}
