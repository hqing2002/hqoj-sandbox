package com.hqing.hqojcodesandbox.model;

import lombok.Data;

/**
 * 判题信息
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗时间(ms)
     */
    private Long time;

    /**
     * 消耗内存(kb)
     */
    private Long memory;
}
