package com.hqing.hqojcodesandbox.core.java;

import com.github.dockerjava.api.model.AccessMode;
import com.hqing.hqojcodesandbox.core.CodeSandboxTemplate;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Java代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "JavaCodeSandbox")
public class JavaCodeSandbox extends CodeSandboxTemplate {
    private static final String CODE_FILE_NAME = "Main.java";

    @Override
    protected String getCodeFileName() {
        return CODE_FILE_NAME;
    }

    @Override
    protected String getCompileCmd(File userCodeFile) {
        String userCodePath = userCodeFile.getAbsolutePath();
        return String.format("javac -encoding utf-8 -J-Dfile.encoding=UTF-8 %s", userCodePath);
    }

    @Override
    protected RunCodeContext buildContext(File userCodeFile, List<String> inputList) {
        RunCodeContext runCodeContext = new RunCodeContext();
        runCodeContext.setUserCodeFile(userCodeFile);
        runCodeContext.setInputList(inputList);
        runCodeContext.setDockerImage("openjdk:8-alpine");
        runCodeContext.setRunCmd(new String[]{"java", "-cp", "/app", "Main"});
        runCodeContext.setAccessMode(AccessMode.ro);
        return runCodeContext;
    }

    @Override
    protected String getErrorMessage(String message) {
        String regex = "/home/.*?\\.java";
        return message.replaceAll(regex, "Main.java");
    }
}
