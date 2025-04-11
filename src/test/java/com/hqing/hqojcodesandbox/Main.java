package com.hqing.hqojcodesandbox;

import com.hqing.hqojcodesandbox.core.CodeSandbox;
import com.hqing.hqojcodesandbox.core.CodeSandboxFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@SpringBootTest
public class Main {
    @Resource
    private CodeSandboxFactory factory;

    @Test
    public void test() {
        CodeSandbox codeSandbox = factory.getInstance("java");
        System.out.println(codeSandbox);
        CodeSandbox codeSandbox1 = factory.getInstance("c");
        System.out.println(codeSandbox1);
        CodeSandbox codeSandbox2 = factory.getInstance("cpp");
        System.out.println(codeSandbox2);
        CodeSandbox codeSandbox3 = factory.getInstance("go");
        System.out.println(codeSandbox3);
        CodeSandbox codeSandbox4 = factory.getInstance("java-native");
        System.out.println(codeSandbox4);
        CodeSandbox codeSandbox5 = factory.getInstance("python");
        System.out.println(codeSandbox5);
    }
}
