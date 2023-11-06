package com.niuma.binapi.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口状态枚举
 *
 * @author niuma
 */
public enum InterfaceInfoAuditStatusEnum {

    PENDING("待审核", 0),
    // 审核通过
    PASS("审核通过", 1),
    // 审核不通过
    FAIL("审核不通过", 2);

    private final String text;

    private final int value;

    InterfaceInfoAuditStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
