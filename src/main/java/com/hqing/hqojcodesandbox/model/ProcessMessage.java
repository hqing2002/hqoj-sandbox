package com.hqing.hqojcodesandbox.model;

import lombok.Data;

/**
 * cmd进程执行信息
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Data
public class ProcessMessage {
    //注意这里不能是int, 会有默认值0, 那直接就是正常退出了
    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;
}
