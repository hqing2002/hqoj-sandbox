package com.hqing.hqojcodesandbox.impl;


import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱调用
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public interface CodeSandbox {
    /**
     * 代码沙箱判题
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
