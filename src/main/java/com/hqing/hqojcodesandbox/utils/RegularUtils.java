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

    public static String replaceBetween(String input, String start, String end, String replacement) {
        if (input == null || start == null || end == null || replacement == null) {
            return input;
        }
        // 转义起始和结束字符串中的特殊字符，并构建正则表达式
        String regex = Pattern.quote(start) + ".*?" + Pattern.quote(end);
        // 使用DOT ALL模式以匹配换行符
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        // 替换所有匹配的部分
        return matcher.replaceAll(Matcher.quoteReplacement(replacement));
    }
}
