package com.hqing.hqojcodesandbox.core;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 策略映射枚举
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Getter
public enum CodeLanguageEnum {
    C("c", "CCodeSandbox"),
    CPP("cpp", "CppCodeSandbox"),
    GO("go", "GoCodeSandbox"),
    JAVA("java", "JavaCodeSandbox"),
    JAVA_NATIVE("java-native", "JavaNativeCodeSandbox"),
    PYTHON("python", "PythonCodeSandbox"),
    DEFAULT("default", "DefaultCodeSandbox");

    private final String languageName;
    private final String beanName;

    CodeLanguageEnum(String languageName, String beanName) {
        this.languageName = languageName;
        this.beanName = beanName;
    }

    /**
     * 根据语言名称获取枚举
     *
     * @param languageName 语言名称
     * @return 策略枚举
     */
    public static CodeLanguageEnum getBeanByLanguageName(String languageName) {
        if (ObjectUtils.isEmpty(languageName)) {
            return DEFAULT;
        }
        for (CodeLanguageEnum strategyEnum : CodeLanguageEnum.values()) {
            if (strategyEnum.getLanguageName().equals(languageName)) {
                return strategyEnum;
            }
        }
        return DEFAULT;
    }

}
