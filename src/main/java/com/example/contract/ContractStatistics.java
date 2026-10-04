package com.example.contract;

/**
 * 汇总统计：契约数、实现数、已执行的 契约 x 实现 组合数、不满足项数。
 * 不满足项 = 失败用例总数 + 漏测组合数 + 版本过期组合数。
 */
public record ContractStatistics(
        int contractCount,
        int implementationCount,
        int executionCount,
        int failedCaseCount,
        int missingRunCount,
        int staleRunCount) {

    public int nonConformingCount() {
        return failedCaseCount + missingRunCount + staleRunCount;
    }

    public boolean allConforming() {
        return nonConformingCount() == 0;
    }

    public String render() {
        return "== 统计 ==\n"
                + "契约数: " + contractCount + '\n'
                + "实现数: " + implementationCount + '\n'
                + "执行组合数: " + executionCount + '\n'
                + "不满足项: " + nonConformingCount()
                + "（失败用例=" + failedCaseCount
                + "，漏测=" + missingRunCount
                + "，版本过期=" + staleRunCount + ")\n";
    }
}
