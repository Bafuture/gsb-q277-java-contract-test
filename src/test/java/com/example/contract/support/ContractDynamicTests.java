package com.example.contract.support;

import static org.junit.jupiter.api.Assertions.fail;

import com.example.contract.CaseResult;
import com.example.contract.Contract;
import com.example.contract.ContractVerifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

/**
 * 把契约用例展开为 JUnit 5 动态测试：每个 用例 x 实现 一个测试节点，
 * 失败时在 IDE / Surefire 报告中直接定位到具体用例。
 */
public final class ContractDynamicTests {

    private ContractDynamicTests() {
    }

    public static <T, I, O> Stream<DynamicTest> forImplementation(
            Contract<T, I, O> contract, String implementationName, T implementation) {
        return contract.cases().stream()
                .map(contractCase -> DynamicTest.dynamicTest(
                        contract.name() + " v" + contract.version()
                                + " / " + implementationName + " / " + contractCase.name(),
                        () -> {
                            CaseResult result =
                                    ContractVerifier.verifyCase(contract, implementation, contractCase);
                            if (!result.isPassed()) {
                                fail(result.failureDetail());
                            }
                        }));
    }
}
