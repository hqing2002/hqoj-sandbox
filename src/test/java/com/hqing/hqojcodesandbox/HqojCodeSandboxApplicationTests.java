package com.hqing.hqojcodesandbox;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class HqojCodeSandboxApplicationTests {
    public static void main(String[] args) {
        int len = Integer.parseInt(args[0]);
        String[] strList = new String[len];
        for (int i = 0; i < len; i++) {
            strList[i] = String.valueOf(i);
        }
        System.out.println(Arrays.toString(strList));
    }
}
