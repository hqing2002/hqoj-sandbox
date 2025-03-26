package com.hqing.hqojcodesandbox.impl.go;

import com.hqing.hqojcodesandbox.impl.CodeSandbox;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component
public class GoDockerCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return null;
    }
}
