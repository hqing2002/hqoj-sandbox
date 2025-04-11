package com.hqing.hqojcodesandbox.core.c;

import com.github.dockerjava.api.model.AccessMode;
import com.hqing.hqojcodesandbox.core.CodeSandboxTemplate;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * c代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "CCodeSandbox")
public class CCodeSandbox extends CodeSandboxTemplate {
    private static final String CODE_FILE_NAME = "Main.c";
    private static final String EXE_FILE_NAME = "main.out";

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
        return String.format("gcc -static %s -o %s", userCodePath, outFilePath);
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
        String regex = "/home/.*?\\.c";
        String filteredContent = message.replaceAll(regex, "Main.c");
        regex = "java.lang.Exception: ";
        filteredContent = filteredContent.replaceAll(regex, "");
        return filteredContent;
    }
}
