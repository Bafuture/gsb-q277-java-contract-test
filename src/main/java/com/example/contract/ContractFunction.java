package com.example.contract;

/**
 * 契约如何调用被测实现：给定实现实例与用例输入，产生输出或抛出异常。
 */
@FunctionalInterface
public interface ContractFunction<T, I, O> {
    O apply(T implementation, I input) throws Exception;
}
