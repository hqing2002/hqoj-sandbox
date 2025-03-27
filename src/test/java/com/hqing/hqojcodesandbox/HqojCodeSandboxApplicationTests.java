package com.hqing.hqojcodesandbox;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HqojCodeSandboxApplicationTests {

    public static void main(String[] args) {
        // JDK 8 兼容的多行字符串写法
        String content = "这是一些其他文本\n"
                + "/home/hqing/code/hqoj-code-sandbox/tmpCode/abc123/Main.java.bat\n"
                + "更多无关内容\n"
                + "/home/hqing/code/hqoj-code-sandbox/tmpCode/invalid-path/Test.java\n"
                + "正确路径：/home/hqing/code/hqoj-code-sandbox/tmpCode/任意目录名/Main.java";

        // 正则表达式（非贪婪匹配）
        String regex = "/home/.*?\\.java";
        // 执行替换操作
        String filteredContent = content.replaceAll(regex, "");

        System.out.println("过滤后的内容：\n" + filteredContent);
    }
}
