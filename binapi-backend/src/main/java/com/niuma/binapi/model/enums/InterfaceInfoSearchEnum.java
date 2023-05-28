package com.niuma.binapi.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author niuma
 * @create 2023-05-28 14:38
 */
public enum InterfaceInfoSearchEnum {
    NATIVE("本地", "native"),
    APIAA1("Apiaa1", "Apiaa1");

    private final String text;

    private final String value;

    InterfaceInfoSearchEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
