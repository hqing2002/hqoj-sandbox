package com.hqing.hqojcodesandbox.core.python;

import com.github.dockerjava.api.model.AccessMode;
import com.hqing.hqojcodesandbox.core.CodeSandboxTemplate;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.utils.RegularUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * c代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "PythonCodeSandbox")
public class PythonCodeSandbox extends CodeSandboxTemplate {
    private static final String CODE_FILE_NAME = "Main.py";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    @Override
    protected boolean enableCompilation() {
        return false;
    }

    @Override
    protected String getCodeFileName() {
        return CODE_FILE_NAME;
    }

    @Override
    protected String getCompileCmd(File userCodeFile) {
        return "";
    }

    @Override
    protected RunCodeContext buildContext(File userCodeFile, List<String> inputList) {
        RunCodeContext runCodeContext = new RunCodeContext();
        runCodeContext.setUserCodeFile(userCodeFile);
        runCodeContext.setInputList(inputList);
        runCodeContext.setDockerImage("python:3.11-alpine");
        runCodeContext.setRunCmd(new String[]{"python", "/app/" + CODE_FILE_NAME});
        runCodeContext.setAccessMode(AccessMode.rw);
        return runCodeContext;
    }

    @Override
    protected String getErrorMessage(String message) {
        return RegularUtils.replaceBetween(message, "java.lang.Exception", "Main.py", "Main.py");
    }
}
