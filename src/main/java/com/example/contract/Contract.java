package com.example.contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一份可复用的契约：名称 + 版本 + 一组用例 + 调用被测实现的方式。
 * 同一份契约可以对任意多个实现复用。
 */
public final class Contract<T, I, O> {

    private final String name;
    private final int version;
    private final List<ContractCase<I, O>> cases;
    private final ContractFunction<T, I, O> function;

    private Contract(Builder<T, I, O> builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.cases = Collections.unmodifiableList(new ArrayList<>(builder.cases));
        this.function = builder.function;
    }

    public static <T, I, O> Builder<T, I, O> define(String name, ContractFunction<T, I, O> function) {
        return new Builder<>(name, function);
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public List<ContractCase<I, O>> cases() {
        return cases;
    }

    public ContractFunction<T, I, O> function() {
        return function;
    }

    public static final class Builder<T, I, O> {
        private final String name;
        private final ContractFunction<T, I, O> function;
        private int version = 1;
        private final List<ContractCase<I, O>> cases = new ArrayList<>();

        private Builder(String name, ContractFunction<T, I, O> function) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("contract name must not be blank");
            }
            if (function == null) {
                throw new IllegalArgumentException("contract function must not be null");
            }
            this.name = name;
            this.function = function;
        }

        public Builder<T, I, O> version(int version) {
            if (version < 1) {
                throw new IllegalArgumentException("contract version must be >= 1");
            }
            this.version = version;
            return this;
        }

        public Builder<T, I, O> caseValue(String caseName, I input, O expected) {
            return addCase(ContractCase.expectingValue(caseName, input, expected));
        }

        public Builder<T, I, O> caseException(
                String caseName, I input, Class<? extends Throwable> exceptionType) {
            return addCase(ContractCase.expectingException(caseName, input, exceptionType));
        }

        public Builder<T, I, O> addCase(ContractCase<I, O> contractCase) {
            cases.add(contractCase);
            return this;
        }

        public Contract<T, I, O> build() {
            if (cases.isEmpty()) {
                throw new IllegalStateException("contract '" + name + "' must declare at least one case");
            }
            return new Contract<>(this);
        }
    }
}
