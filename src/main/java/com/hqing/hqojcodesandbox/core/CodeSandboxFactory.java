package com.hqing.hqojcodesandbox.core;

import com.hqing.hqojcodesandbox.core.cpp.CppCodeSandbox;
import com.hqing.hqojcodesandbox.core.go.GoDockerCodeSandbox;
import com.hqing.hqojcodesandbox.core.java.JavaCodeSandbox;
import com.hqing.hqojcodesandbox.core.java.JavaNativeCodeSandbox;
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
    private JavaCodeSandbox javaCodeSandbox;

    @Resource
    private CppCodeSandbox cppCodeSandbox;

    @Resource
    private GoDockerCodeSandbox goDockerCodeSandbox;

    @Resource
    private DefaultCodeSandbox defaultCodeSandbox;

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    public CodeSandbox newInstance(String type) {
        switch (type) {
            case "java":
                return javaCodeSandbox;
            case "java-native":
                return javaNativeCodeSandbox;
            case "cpp":
                return cppCodeSandbox;
            case "go":
                return goDockerCodeSandbox;
            default:
                return defaultCodeSandbox;
        }
    }
}
