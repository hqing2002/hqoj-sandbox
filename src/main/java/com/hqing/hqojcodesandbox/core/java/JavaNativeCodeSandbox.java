package com.hqing.hqojcodesandbox.core.java;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.model.AccessMode;
import com.hqing.hqojcodesandbox.core.CodeSandboxTemplate;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeStrategyEnum;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Java代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component
public class JavaNativeCodeSandbox  extends JavaCodeSandbox {
    //定义字典树存储黑名单
    private static final WordTree BLACK_KEY_LIST;

    //在静态代码块中初始化字典树
    static {
        BLACK_KEY_LIST = new WordTree();
        BLACK_KEY_LIST.addWords("File", "exec", "dir", "Dir", "Thread", "write", "bat", "path", "sleep", "Process");
    }

    @Override
    protected RunCodeStrategyEnum getRunCodeStrategyEnum() {
        return RunCodeStrategyEnum.NATIVE;
    }

    @Override
    protected RunCodeContext buildContext(File userCodeFile, List<String> inputList) {
        RunCodeContext runCodeContext = super.buildContext(userCodeFile, inputList);
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        runCodeContext.setRunCmd(new String[]{"java", "-cp", userCodeParentPath, "Main"});
        return runCodeContext;
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
