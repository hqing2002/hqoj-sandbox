package com.hqing.hqojcodesandbox.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码沙箱工厂(根据传入的字符串参数进行创建沙箱实现实例)
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Slf4j
@Service
public class CodeSandboxFactory {
    private final Map<String, CodeSandbox> codeSandboxMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        //@PostConstruct:在对象创建后执行的初始化逻辑, 使用ApplicationContext注入所有CodeSandbox实现类
        codeSandboxMap.putAll(applicationContext.getBeansOfType(CodeSandbox.class));
    }

    public CodeSandbox getInstance(String languageName) {
        CodeLanguageEnum languageEnum = CodeLanguageEnum.getBeanByLanguageName(languageName);
        String beanName = languageEnum.getBeanName();
        if (StringUtils.isEmpty(beanName)) {
            log.error("策略管理器获取Bean失败, 枚举类名称配置错误");
            return null;
        }
        return codeSandboxMap.get(beanName);
    }
}
