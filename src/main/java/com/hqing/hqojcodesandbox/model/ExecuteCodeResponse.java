package com.hqing.hqojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱返回参数
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    /**
     * 代码输出
     */
    private List<String> outputList;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 消耗时间(ms)
     */
    private Long time;

    /**
     * 消耗内存(kb)
     */
    private Long memory;
}
