package com.hqing.hqojcodesandbox.impl;

import com.hqing.hqojcodesandbox.impl.cpp.CppDockerCodeSandbox;
import com.hqing.hqojcodesandbox.impl.go.GoDockerCodeSandbox;
import com.hqing.hqojcodesandbox.impl.java.JavaDockerCodeSandbox;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 代码沙箱工厂(根据传入的字符串参数进行创建沙箱实现实例)
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component
public class CodeSandboxFactory {
    /**
     * 创造代码沙箱实例
     *
     * @param type 沙箱类型
     * @return
     */
    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    @Resource
    private CppDockerCodeSandbox cppDockerCodeSandbox;

    @Resource
    private GoDockerCodeSandbox goDockerCodeSandbox;

    @Resource
    private DefaultCodeSandbox defaultCodeSandbox;

    public CodeSandbox newInstance(String type) {
        switch (type) {
            case "java":
                return javaDockerCodeSandbox;
            case "cpp":
                return cppDockerCodeSandbox;
            case "go":
                return goDockerCodeSandbox;
            default:
                return defaultCodeSandbox;
        }
    }
}
