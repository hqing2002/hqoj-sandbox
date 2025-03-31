package com.hqing.hqojcodesandbox.core.java;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.hqing.hqojcodesandbox.core.CodeSandboxTemplate;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeStrategyEnum;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Java代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component
public class JavaNativeCodeSandbox extends CodeSandboxTemplate {
    //定义字典树存储黑名单
    private static final WordTree BLACK_KEY_LIST;

    private static final String CODE_FILE_NAME = "Main.java";

    //在静态代码块中初始化字典树
    static {
        BLACK_KEY_LIST = new WordTree();
        BLACK_KEY_LIST.addWords("File", "exec", "dir", "Dir", "Thread", "write", "bat", "path", "sleep", "Process");
    }

    @Override
    protected String getCodeFileName() {
        return CODE_FILE_NAME;
    }

    @Override
    protected RunCodeStrategyEnum getRunCodeStrategyEnum() {
        return RunCodeStrategyEnum.NATIVE;
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
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        runCodeContext.setRunCmd(new String[]{"java", "-Xmx256m", "-Dfile.encoding=UTF-8", "-cp", userCodeParentPath, "Main"});
        return runCodeContext;
    }

    @Override
    protected String getErrorMessage(String message) {
        String regex = "/home/.*?\\.java";
        return message.replaceAll(regex, "Main.java");
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //Java原生实现先校验字典树
        String code = executeCodeRequest.getCode();
        FoundWord foundWord = BLACK_KEY_LIST.matchWord(code);
        //如果存在违禁词
        if (foundWord != null) {
            System.out.println("包含禁止词:" + foundWord.getFoundWord());
            return getErrorResponse(new Throwable("代码包含禁止词:" + foundWord.getFoundWord()));
        }
        return super.executeCode(executeCodeRequest);
    }
}
