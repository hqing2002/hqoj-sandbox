package com.hqing.hqojcodesandbox.impl.java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.hqing.hqojcodesandbox.impl.CodeSandbox;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.model.SandboxResponseStatusEnum;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码沙箱执行流程模板
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Resource
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {
    //将全局用到的字符串(魔法值)都写成字符串常量, 便于维护
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    private static volatile Boolean GLOBAL_CODE_DIR_NOT_EXIST = true;

    /**
     * 1.把用户代码保存为文件
     *
     * @param code 用户代码
     * @return 返回创建的文件对象
     */
    protected File saveCodetoFile(String code) {
        //获取工作目录(在IDE中为项目根目录)
        String userDir = System.getProperty("user.dir");

        //这里用File.separator作为分隔符而不是直接写'\\'是因为在linux和windows上分隔符不一样, 用这个可以自动识别兼容不同系统
        //globalCodePathName: userDir/tempCode
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        //判断全局代码目录是否存在, 没有则新建
        if (GLOBAL_CODE_DIR_NOT_EXIST) {
            synchronized (JavaCodeSandboxTemplate.class) {
                if (GLOBAL_CODE_DIR_NOT_EXIST && !FileUtil.exist(globalCodePathName)) {
                    System.out.println("创建用户代码目录成功");
                    FileUtil.mkdir(globalCodePathName);
                    GLOBAL_CODE_DIR_NOT_EXIST = false;
                }
            }
        }

        //把用户的代码隔离到文件夹中, 命名用UUID
        //userCodePath: userDir/tempCode/UUID/Main.java
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 2.编译代码得到class文件
     *
     * @param userCodeFile 用户代码文件
     * @return 封装的cmd输出信息
     */
    protected ExecuteMessage compileCodeFile(File userCodeFile) throws Exception {
        String compileCmd = String.format("javac -encoding utf-8 -J-Dfile.encoding=UTF-8 %s", userCodeFile.getAbsolutePath());
        try {
            //执行cmd命令
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(compileProcess, "编译");
            System.out.println("编译结束, 耗时: " + executeMessage.getTime());
            String errorMessage = executeMessage.getErrorMessage();
            //编译有错误信息
            if (StrUtil.isNotBlank(errorMessage)) {
                throw new Exception(errorMessage);
            }
            return executeMessage;
        } catch (Exception e) {
            //CMD命令执行报错
            throw new Exception(e);
        }
    }

    /**
     * 3.执行代码, 获得执行结果列表, 定义为抽象方法子类必须实现
     *
     * @param userCodeFile 用户代码文件
     * @param inputList    输入用例
     * @return 执行结果列表
     */
    protected abstract List<ExecuteMessage> runCodeFile(File userCodeFile, List<String> inputList) throws Exception;

    /**
     * 4.根据执行结果列表封装输出结果
     *
     * @param executeMessageList 执行结果列表
     * @return 沙箱输出
     */
    protected ExecuteCodeResponse getOutPutResponse(List<ExecuteMessage> executeMessageList) {
        List<String> outputList = new ArrayList<>();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        //取最大值判断是否超时
        long maxTime = 0L;
        long maxMemory = 0L;

        for (ExecuteMessage executeMessage : executeMessageList) {
            Long time = executeMessage.getTime();
            Long memory = executeMessage.getMemory();
            String message = executeMessage.getMessage();
            String errorMessage = executeMessage.getErrorMessage();
            Integer exitValue = executeMessage.getExitValue();

            //如果运行中有报错
            if (exitValue != 0 || StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(SandboxResponseStatusEnum.RUNTIME_ERROR.getValue());
                break;
            }
            outputList.add(message.trim());
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }

        //正常完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(SandboxResponseStatusEnum.ACCEPT.getValue());
        }
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setTime(maxTime);
        executeCodeResponse.setMemory(maxMemory);
        System.out.println("docker程序执行结束");
        return executeCodeResponse;
    }


    /**
     * 5. 文件清理
     *
     * @param userCodeFile 用户的代码文件
     */
    protected void deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
    }

    /**
     * 6.返回错误响应信息
     *
     * @param e 错误信息
     * @return 返回错误响应
     */
    protected ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        String regex = "/home/.*?\\.java";
        String filteredContent = e.getMessage().replaceAll(regex, "Main.java");
        executeCodeResponse.setMessage(filteredContent);
        //表示代码沙箱错误(编译错误)
        executeCodeResponse.setStatus(SandboxResponseStatusEnum.COMPILE_ERROR.getValue());
        return executeCodeResponse;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        //1. 把用户代码存入文件
        File userCodeFile = saveCodetoFile(code);
        try {
            //2. 编译用户代码, 得到class文件
            ExecuteMessage compileExecuteMessage = compileCodeFile(userCodeFile);

            //3. 运行代码, 得到代码用例运行结果
            List<ExecuteMessage> executeMessageList = runCodeFile(userCodeFile, inputList);

            //4.整理输出结果, 将运行结果列表封装成沙箱响应
            return getOutPutResponse(executeMessageList);
        } catch (Exception e) {
            return getErrorResponse(e);
        } finally {
            deleteFile(userCodeFile);
        }
    }
}
