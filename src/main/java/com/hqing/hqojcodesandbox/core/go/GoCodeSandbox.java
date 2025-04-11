package com.hqing.hqojcodesandbox.core.go;


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
 * go代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "GoCodeSandbox")
public class GoCodeSandbox extends CodeSandboxTemplate {
    private static final String CODE_FILE_NAME = "Main.go";
    private static final String EXE_FILE_NAME = "main";
    private static final String GO_PATH = "/usr/local/go/bin/";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    @Override
    protected String getCodeFileName() {
        return CODE_FILE_NAME;
    }

    @Override
    protected String getCompileCmd(File userCodeFile) {
        String userCodePath = userCodeFile.getAbsolutePath();
        String outFilePath = userCodeFile.getParentFile().getAbsolutePath() + File.separator + EXE_FILE_NAME;
        return String.format("%sgo build -o %s %s", GO_PATH, outFilePath, userCodePath);
    }

    @Override
    protected RunCodeContext buildContext(File userCodeFile, List<String> inputList) {
        RunCodeContext runCodeContext = new RunCodeContext();
        runCodeContext.setUserCodeFile(userCodeFile);
        runCodeContext.setInputList(inputList);
        runCodeContext.setDockerImage("alpine:3.21");
        runCodeContext.setRunCmd(new String[]{"/app/" + EXE_FILE_NAME});
        runCodeContext.setAccessMode(AccessMode.rw);
        return runCodeContext;
    }

    @Override
    protected String getErrorMessage(String message) {
        return RegularUtils.replaceBetween(message, "java.lang.Exception", "Main.go", "Main.go");
    }
}
