package com.hqing.hqojcodesandbox;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HqojCodeSandboxApplicationTests {

    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("结果: " + (a + b));
    }
}
