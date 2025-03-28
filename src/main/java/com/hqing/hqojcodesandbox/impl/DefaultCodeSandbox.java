package com.hqing.hqojcodesandbox.impl;

import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.SandboxResponseStatusEnum;

import java.util.ArrayList;

/**
 * 默认沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class DefaultCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage("编程语言暂不支持");
        executeCodeResponse.setStatus(SandboxResponseStatusEnum.COMPILE_ERROR.getValue());
        executeCodeResponse.setTime(0L);
        executeCodeResponse.setMemory(0L);
        return executeCodeResponse;
    }
}
