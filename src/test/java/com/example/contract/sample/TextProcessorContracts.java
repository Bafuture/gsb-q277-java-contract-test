package com.example.contract.sample;

import com.example.contract.Contract;

/** 示例契约定义。 */
public final class TextProcessorContracts {

    private TextProcessorContracts() {
    }

    public static Contract<TextProcessor, String, String> v1() {
        return Contract.<TextProcessor, String, String>define(
                        "text-processor", TextProcessor::process)
                .version(1)
                .caseValue("trims and uppercases", "  hello  ", "HELLO")
                .caseValue("already clean input", "abc", "ABC")
                .caseException("blank input rejected", "   ", IllegalArgumentException.class)
                .build();
    }

    /** 契约演进：v2 新增一个用例。 */
    public static Contract<TextProcessor, String, String> v2() {
        return Contract.<TextProcessor, String, String>define(
                        "text-processor", TextProcessor::process)
                .version(2)
                .caseValue("trims and uppercases", "  hello  ", "HELLO")
                .caseValue("already clean input", "abc", "ABC")
                .caseValue("mixed case", "aBc", "ABC")
                .caseException("blank input rejected", "   ", IllegalArgumentException.class)
                .build();
    }
}
