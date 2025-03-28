package com.hqing.hqojcodesandbox;

import cn.hutool.core.collection.ListUtil;

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
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("123");
        List<String> sub = ListUtil.sub(list, 1, 10);
        System.out.println(sub);
    }
}
