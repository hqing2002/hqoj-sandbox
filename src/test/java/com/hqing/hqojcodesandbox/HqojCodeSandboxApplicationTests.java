package com.hqing.hqojcodesandbox;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.Charset;

@SpringBootTest
class HqojCodeSandboxApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        System.out.println("操作系统: " + System.getProperty("os.name"));
        System.out.println("JVM 默认编码: " + Charset.defaultCharset().displayName());
        System.out.println("测试输出: 中文 English Español");
    }
}
