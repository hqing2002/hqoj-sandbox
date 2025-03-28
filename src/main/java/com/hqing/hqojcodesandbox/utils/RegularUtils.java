package com.hqing.hqojcodesandbox.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class RegularUtils {
    public static String[] parseArguments(String input) {
        List<String> args = new ArrayList<>();
        if (input.isEmpty()) {
            // 显式处理空输入
            args.add("");
        } else {
            Pattern pattern = Pattern.compile("\"([^\"]*)\"|([^\\s\"]+)");
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                String arg = (matcher.group(1) != null) ?
                        matcher.group(1) :
                        matcher.group(2);
                args.add(arg);
            }
        }
        return args.toArray(new String[0]);
    }
}
