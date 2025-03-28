package com.hqing.hqojcodesandbox.model;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 沙箱接口返回状态枚举
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public enum SandboxResponseStatusEnum {
    ACCEPT("成功", 1),
    COMPILE_ERROR("编译失败", 2),
    RUNTIME_ERROR("运行失败", 3);

    private final String text;

    private final Integer value;

    SandboxResponseStatusEnum(String text, Integer value) {
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

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SandboxResponseStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SandboxResponseStatusEnum anEnum : SandboxResponseStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
