package com.hqing.hqojcodesandbox.core;


import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱调用接口
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public interface CodeSandbox {
    /**
     * 代码沙箱判题
     *
     * @param executeCodeRequest 请求信息
     * @return 执行结果
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
