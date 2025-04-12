package com.hqing.hqojcodesandbox.strategy.model;

import lombok.Data;

/**
 * cmd进程执行信息
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Data
public class RunCodeResult {
    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
