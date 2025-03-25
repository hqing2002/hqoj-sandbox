package com.hqing.hqojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ProcessMessage;
import com.hqing.hqojcodesandbox.model.JudgeInfo;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class JavaNativeCodeSandbox implements CodeSandbox {
    //将全局用到的字符串(魔法值)都写成字符串常量, 便于维护
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    //定义程序最大运行时长
    private static final long MAX_TIME_LIMIT = 5000L;

    //定义字典树存储黑名单
    private static final WordTree BLACK_KEY_LIST;

    private static final String SECURITY_MANAGER_PATH = "D:\\Code\\hqing\\hqoj\\hqoj-code-sandbox\\src\\main\\resources\\testCode\\security";
    private static final String SECURITY_CLASS_NAME = "MySecurityManager";

    //在静态代码块中初始化字典树
    static {
        BLACK_KEY_LIST = new WordTree();
        BLACK_KEY_LIST.addWords("File", "exec", "dir", "Dir", "Thread", "write", "bat", "path", "sleep", "Process");
    }

    //创建main方法进行测试
    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();

        //读取resource目录下的main.java用于测试
        String path = "testCode/unsafeCode/RunFileError.java";
        String code = ResourceUtil.readStr(path, StandardCharsets.UTF_8);
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);

    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //0. 代码校验
        FoundWord foundWord = BLACK_KEY_LIST.matchWord(code);
        //如果存在违禁词
        if (foundWord != null) {
            System.out.println("包含禁止词:" + foundWord.getFoundWord());
            return null;
        }

        //1. 把用户代码存入文件
        //获取工作目录(在IDE中为项目根目录)
        String userDir = System.getProperty("user.dir");

        //这里用File.separator作为分隔符而不是直接写'\\'是因为在linux和windows上分隔符不一样, 用这个可以自动识别兼容不同系统
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        //判断全局代码目录是否存在, 没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        //把用户的代码隔离到文件夹中, 命名用UUID
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        //2. 编译用户代码, 得到class文件
        String compileCmd = String.format("javac -encoding utf-8 -J-Dfile.encoding=UTF-8 %s", userCodeFile.getAbsolutePath());
        try {
            //执行cmd命令
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ProcessMessage processMessage = ProcessUtils.getProcessMessage(compileProcess, "编译");
            System.out.println(processMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        //3. 执行代码, 得到输出信息
        List<ProcessMessage> processMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_CLASS_NAME, inputArgs);
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
                ProcessMessage processMessage = ProcessUtils.getProcessMessage(runProcess, "运行");
                System.out.println(processMessage);
                processMessageList.add(processMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }

        //4.收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();

        //取最大值判断是否超时
        long maxTime = 0L;

        for (ProcessMessage processMessage : processMessageList) {
            Long time = processMessage.getTime();
            String message = processMessage.getMessage();
            String errorMessage = processMessage.getErrorMessage();

            //如果运行中有报错
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(message);
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }

        //正常完成
        if (outputList.size() == processMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //这里不统计内存, 先写个假数据
        judgeInfo.setMemory(-1L);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //5. 文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误(编译错误)
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
