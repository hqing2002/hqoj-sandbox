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
        Pattern pattern = Pattern.compile("([^\"\\s]+|\"[^\"]*\")\\s*");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String arg = matcher.group(1);
            // 去除引号并处理转义（可选）
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            args.add(arg);
        }
        return args.toArray(new String[0]);
    }
}
