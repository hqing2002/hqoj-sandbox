package com.hqing.hqojcodesandbox;

import cn.hutool.core.collection.ListUtil;
import com.hqing.hqojcodesandbox.utils.RegularUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class Main {
    public static void main(String[] args) {
        String s = "\"\"";
        String[] strings = RegularUtils.parseArguments(s);
        System.out.println(strings.length);
        for (String string : strings) {
            System.out.println(string);
        }
    }
}
