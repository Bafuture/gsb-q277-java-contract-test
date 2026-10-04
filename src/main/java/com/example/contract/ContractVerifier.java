package com.example.contract;

import java.util.Objects;

/**
 * 契约执行器：把一份契约的全部（或单个）用例跑到某个实现上。
 */
public final class ContractVerifier {

    private ContractVerifier() {
    }

    public static <T, I, O> ContractVerification verify(
            Contract<T, I, O> contract, String implementationName, T implementation) {
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(implementationName, "implementationName");
        java.util.List<CaseResult> results = new java.util.ArrayList<>(contract.cases().size());
        for (ContractCase<I, O> contractCase : contract.cases()) {
            results.add(verifyCase(contract, implementation, contractCase));
        }
        return new ContractVerification(contract.name(), contract.version(), implementationName, results);
    }

    public static <T, I, O> CaseResult verifyCase(
            Contract<T, I, O> contract, T implementation, ContractCase<I, O> contractCase) {
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(contractCase, "contractCase");
        Expectation<O> expectation = contractCase.expectation();
        O returnedValue = null;
        Throwable thrown = null;
        try {
            returnedValue = contract.function().apply(implementation, contractCase.input());
        } catch (Throwable t) {
            thrown = t;
        }

        String caseName = contractCase.name();
        String expected = expectation.describe();

        if (expectation.isException()) {
            Class<? extends Throwable> expectedType = expectation.expectedExceptionType();
            if (thrown == null) {
                return CaseResult.failed(
                        caseName,
                        expected,
                        returnedValue,
                        null,
                        "case '" + caseName + "' expected " + expected
                                + " but implementation returned <" + returnedValue + ">");
            }
            if (!expectedType.isInstance(thrown)) {
                return CaseResult.failed(
                        caseName,
                        expected,
                        null,
                        thrown,
                        "case '" + caseName + "' expected " + expected
                                + " but implementation threw " + thrown.getClass().getName()
                                + ": " + thrown.getMessage());
            }
            return CaseResult.passedException(caseName, expected, thrown.getClass());
        }

        if (thrown != null) {
            return CaseResult.failed(
                    caseName,
                    expected,
                    null,
                    thrown,
                    "case '" + caseName + "' expected " + expected
                            + " but implementation threw " + thrown.getClass().getName()
                            + ": " + thrown.getMessage());
        }
        if (!Objects.equals(expectation.expectedValue(), returnedValue)) {
            return CaseResult.failed(
                    caseName,
                    expected,
                    returnedValue,
                    null,
                    "case '" + caseName + "' expected " + expected
                            + " but got <" + returnedValue + ">");
        }
        return CaseResult.passed(caseName, expected, returnedValue);
    }
}
