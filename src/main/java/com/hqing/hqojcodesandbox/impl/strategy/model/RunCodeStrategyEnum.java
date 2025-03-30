package com.hqing.hqojcodesandbox.impl.strategy.model;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 策略映射枚举
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public enum RunCodeStrategyEnum {
    DOCKER("DockerRunner"),
    NATIVE("NativeRunner");

    @Getter
    private final String strategyName;


    RunCodeStrategyEnum(String strategyName) {
        this.strategyName = strategyName;
    }

    /**
     * 根据策略名称获取枚举
     *
     * @param strategyName 策略名称
     * @return 策略枚举
     */
    public static RunCodeStrategyEnum getByStrategyName(String strategyName) {
        if (ObjectUtils.isEmpty(strategyName)) {
            return null;
        }
        for (RunCodeStrategyEnum strategyEnum : RunCodeStrategyEnum.values()) {
            if (strategyEnum.getStrategyName().equals(strategyName)) {
                return strategyEnum;
            }
        }
        return null;
    }

}
