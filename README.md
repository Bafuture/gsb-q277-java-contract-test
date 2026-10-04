# 契约测试框架

Pair-wise GSB 标注任务仓库（第 17 批 / 277）。

| 项目 | 内容 |
|------|------|
| 任务类型 | Feature 迭代 |
| 任务难度 | 困难 |
| 语言/框架 | Java, Maven, JUnit 5 |
| 环境可复现等级 | 无外部依赖 |
| 构建方式 | Maven（含 mvnw wrapper，无需本机安装 Maven） |

> 本仓库是**初始环境快照**：只有工程骨架，不含任何实现代码。
> 分支说明：`main` 为初始环境；`A`、`B` 为两次独立执行各自的工作分支，均从 `main` 的同一个提交拉出。

## 运行方式

```bash
./mvnw -q verify
```

## 框架用法

核心 API 位于 `com.example.contract` 包：

```java
// 1. 定义契约用例（输入 + 预期输出 / 预期异常）并组成带版本的契约
Contract<String, String> contract = new Contract<>("text-normalizer", 1, List.of(
        ContractCase.expectingOutput("trims-whitespace", "  hello  ", "hello"),
        ContractCase.expectingException("null-input-throws-npe", null, NullPointerException.class)));

// 2. 注册契约与多个实现（任意实现类通过方法引用适配）
ContractRegistry registry = new ContractRegistry();
registry.registerContract(contract);
registry.registerImplementation("text-normalizer",
        Implementation.of("trim", new TrimNormalizer()::normalize));
registry.registerImplementation("text-normalizer",
        Implementation.of("strip", new StripNormalizer()::normalize));

// 3. 自动对全部实现跑全部契约用例，失败定位到具体用例
List<ContractExecution> executions = registry.verifyAll();

// 4. 覆盖报告：漏测（MISSING）、版本过期（STALE）、通过/失败/未执行数、
//    跨实现一致性（同一输入异常类型或输出不一致）与统计信息
CoverageReport report = registry.report();
System.out.println(report.summary());
```

- 契约变更后用 `registry.registerContract(contract.withVersion(2))` 升级版本，
  之前按旧版本验证过的实现会在报告中标记为 `STALE`。
- `report.statistics()` 提供契约数、实现数、执行组合数与不满足项总数。

## 任务提示词

以下为本题完整的 User Prompt 原文，两次执行必须使用完全相同的文本。

我们有多套接口实现，需要保证它们对同一组契约的行为一致，但手工写重复用例太费事。请从零实现一个契约测试框架。仓库目前只有一个空的 Maven 工程（pom.xml 只声明 JUnit 5 与 AssertJ）。要求：1) 支持定义契约用例（输入、预期输出或预期异常），契约可复用于多个实现；2) 支持对实现类自动跑全部契约用例，任何实现不满足契约都要定位到具体用例；3) 支持实现清单自动发现：注册的实现若缺少契约执行记录，要能报告漏测；4) 支持契约版本：契约变更后要能识别哪些实现还未按新契约验证过；5) 支持契约覆盖报告：每个实现通过、失败、未执行的用例数；6) 支持异常契约：同一输入不同实现返回不同异常类型时应视为不满足契约，并输出实际异常；7) 提供统计：契约数、实现数、执行组合数与不满足项；8) 测试覆盖多实现一致性检查、失败定位、漏测报告、契约版本变更、异常契约与统计；`mvn -q verify` 一条命令跑通。

## 提交要求

1. 在本仓库中完成提示词要求的全部内容。
2. `./mvnw -q verify` 必须通过。
3. 完成后在所属分支（A 或 B）上提交，产物快照的父提交必须是初始环境快照。
