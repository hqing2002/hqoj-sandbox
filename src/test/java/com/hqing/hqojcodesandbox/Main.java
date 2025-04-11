package com.hqing.hqojcodesandbox;

import com.hqing.hqojcodesandbox.core.CodeSandbox;
import com.hqing.hqojcodesandbox.core.CodeSandboxFactory;
import com.hqing.hqojcodesandbox.utils.RegularUtils;
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

    }

    public static void main(String[] args) {
        String s = RegularUtils.replaceBetween("1273*axxx\nxx\\' | .c123", "a", ".c", "hhh");
        System.out.println(s);
    }
}
