package com.hqing.hqojcodesandbox.strategy;

import com.hqing.hqojcodesandbox.strategy.model.RunCodeStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 策略管理器
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Slf4j
@Service
public class RunStrategyManager {
    private final Map<String, RunStrategy> strategyMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        //@PostConstruct:在对象创建后执行的初始化逻辑, 使用ApplicationContext注入所有RunStrategy的实现类
        strategyMap.putAll(applicationContext.getBeansOfType(RunStrategy.class));
    }

    public RunStrategy getInstance(String runStrategyName) {
        RunCodeStrategyEnum strategyEnum = RunCodeStrategyEnum.getByStrategyName(runStrategyName);
        if (strategyEnum == null) {
            log.error("调用策略管理器失败, 没有找到相应名称的策略");
            return null;
        }
        return getInstanceByBeanName(strategyEnum.getStrategyName());
    }

    private RunStrategy getInstanceByBeanName(String beanName) {
        if (StringUtils.isEmpty(beanName)) {
            log.error("策略管理器获取Bean失败, 枚举类名称配置错误");
            return null;
        }
        return strategyMap.get(beanName);
    }
}
