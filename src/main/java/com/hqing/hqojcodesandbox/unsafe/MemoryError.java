package com.hqing.hqojcodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用空间（浪费系统内存）
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class MemoryError {
    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[10000]);
        }
    }
}
