package com.hqing.hqojcodesandbox.impl.java;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Java代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    //定义程序最大运行时长
    private static final long MAX_TIME_LIMIT = 5000L;

    //定义字典树存储黑名单
    private static final WordTree BLACK_KEY_LIST;

    //在静态代码块中初始化字典树
    static {
        BLACK_KEY_LIST = new WordTree();
        BLACK_KEY_LIST.addWords("File", "exec", "dir", "Dir", "Thread", "write", "bat", "path", "sleep", "Process");
    }

    @Override
    protected List<ExecuteMessage> runCodeFile(File userCodeFile, List<String> inputList) throws Exception {
        //执行代码, 得到输出信息
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                //执行cmd命令
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //开启时间守护线程
                new Thread(() -> {
                    try {
                        Thread.sleep(MAX_TIME_LIMIT);
                        if (runProcess.isAlive()) {
                            System.out.println("运行超时了, 中断");
                            runProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
        return executeMessageList;
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
