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

## 任务提示词

以下为本题完整的 User Prompt 原文，两次执行必须使用完全相同的文本。

我们有多套接口实现，需要保证它们对同一组契约的行为一致，但手工写重复用例太费事。请从零实现一个契约测试框架。仓库目前只有一个空的 Maven 工程（pom.xml 只声明 JUnit 5 与 AssertJ）。要求：1) 支持定义契约用例（输入、预期输出或预期异常），契约可复用于多个实现；2) 支持对实现类自动跑全部契约用例，任何实现不满足契约都要定位到具体用例；3) 支持实现清单自动发现：注册的实现若缺少契约执行记录，要能报告漏测；4) 支持契约版本：契约变更后要能识别哪些实现还未按新契约验证过；5) 支持契约覆盖报告：每个实现通过、失败、未执行的用例数；6) 支持异常契约：同一输入不同实现返回不同异常类型时应视为不满足契约，并输出实际异常；7) 提供统计：契约数、实现数、执行组合数与不满足项；8) 测试覆盖多实现一致性检查、失败定位、漏测报告、契约版本变更、异常契约与统计；`mvn -q verify` 一条命令跑通。

## 提交要求

1. 在本仓库中完成提示词要求的全部内容。
2. `./mvnw -q verify` 必须通过。
3. 完成后在所属分支（A 或 B）上提交，产物快照的父提交必须是初始环境快照。

---

## 框架使用说明（本次实现）

核心代码位于 `src/main/java/com/example/contract/`：

| 类 | 作用 |
|----|------|
| `Contract` / `ContractCase` / `Expectation` | 定义契约：名称、版本、用例（输入 + 预期值或预期异常类型），可复用于多个实现 |
| `ContractVerifier` / `ContractVerification` / `CaseResult` | 对单个实现执行全部用例，失败时携带用例名、期望与实际值/实际异常 |
| `ContractSuite` | 登记契约与实现清单，`verifyAll()` 跑全部组合并留存记录 |
| `CoverageReport` | 每个 契约×实现 的通过/失败/未执行用例数；识别漏测与版本过期 |
| `ConsistencyReport` | 跨实现一致性：同一用例的返回值或异常类型必须一致，不一致时输出各实现实际行为 |
| `ContractStatistics` | 契约数、实现数、执行组合数、不满足项（失败用例 + 漏测 + 版本过期） |

示例：

```java
Contract<TextProcessor, String, String> contract =
        Contract.<TextProcessor, String, String>define("text-processor", TextProcessor::process)
                .version(1)
                .caseValue("trims and uppercases", "  hello  ", "HELLO")
                .caseException("blank input rejected", "   ", IllegalArgumentException.class)
                .build();

ContractSuite suite = new ContractSuite()
        .registerContract(contract)
        .registerImplementation("standard", new StandardTextProcessor())
        .registerImplementation("legacy", new LegacyTextProcessor());

suite.verifyAll();
System.out.println(suite.coverageReport().render());
System.out.println(suite.consistencyReport("text-processor").render());
System.out.println(suite.statistics().render());
```

契约升级时以同名 `registerContract` 注册新版本即可，未重新验证的实现会在覆盖报告中标记为「版本过期」。测试侧 `support/ContractDynamicTests` 可把用例展开为 JUnit 5 `@TestFactory` 动态测试，在 IDE/报告中按用例定位失败。
