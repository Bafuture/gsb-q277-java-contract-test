package com.example.contract.sample;

/** 示例接口：trim 后转大写；空白输入必须抛 IllegalArgumentException。 */
public interface TextProcessor {
    String process(String input);
}
