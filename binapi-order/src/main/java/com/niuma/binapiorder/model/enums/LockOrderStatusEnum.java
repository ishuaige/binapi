package com.niuma.binapiorder.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态枚举类
 * @author niuma
 * @create 2023-05-03 20:19
 */
public enum LockOrderStatusEnum {

    UNLOCK("已解锁", 0),

    LOCK("已锁定", 1),
    DEDUCT("已扣减", 2);

    private final String text;

    private final int value;

    LockOrderStatusEnum(String text, int value) {
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
