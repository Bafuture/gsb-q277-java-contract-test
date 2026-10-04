package com.example.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runs every case of a contract against one implementation and produces
 * a {@link ContractExecution} recording the per-case outcomes.
 */
public final class ContractRunner {

    public <I, O> ContractExecution run(Contract<I, O> contract, Implementation<I, O> implementation) {
        List<CaseResult> results = new ArrayList<>();
        for (ContractCase<I, O> contractCase : contract.cases()) {
            results.add(runCase(contractCase, implementation));
        }
        return new ContractExecution(contract.name(), contract.version(), implementation.name(), results);
    }

    private <I, O> CaseResult runCase(ContractCase<I, O> contractCase, Implementation<I, O> implementation) {
        Object output = null;
        Throwable thrown = null;
        try {
            output = implementation.invoke(contractCase.input());
        } catch (Throwable t) {
            thrown = t;
        }

        String expectation = contractCase.expectationDescription();
        if (contractCase.expectsException()) {
            if (thrown == null) {
                return CaseResult.failed(contractCase.id(), expectation, output, null);
            }
            if (!contractCase.expectedException().isInstance(thrown)) {
                return CaseResult.failed(contractCase.id(), expectation, null, thrown);
            }
            return CaseResult.passed(contractCase.id(), expectation, null, thrown);
        }

        if (thrown != null) {
            return CaseResult.failed(contractCase.id(), expectation, null, thrown);
        }
        if (!Objects.equals(output, contractCase.expectedOutput())) {
            return CaseResult.failed(contractCase.id(), expectation, output, null);
        }
        return CaseResult.passed(contractCase.id(), expectation, output, null);
    }
}
