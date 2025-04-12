package com.hqing.hqojcodesandbox.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.model.SandboxResponseStatusEnum;
import com.hqing.hqojcodesandbox.strategy.RunStrategy;
import com.hqing.hqojcodesandbox.strategy.RunStrategyManager;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeStrategyEnum;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码沙箱模板(抽离核心流程)
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public abstract class CodeSandboxTemplate implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static volatile Boolean GLOBAL_CODE_DIR_NOT_EXIST = true;

    @Resource
    private RunStrategyManager runStrategyManager;

    /**
     * 获取代码文件名
     */
    protected abstract String getCodeFileName();

    /**
     * 是否启用编译阶段（默认true）
     * 解释型语言可关闭编译步骤
     */
    protected boolean enableCompilation() {
        return true;
    }

    /**
     * 获取编译命令
     */
    protected abstract String getCompileCmd(File userCodeFile);

    /**
     * 子类选择需要的沙箱类型, 默认Docker
     */
    protected RunCodeStrategyEnum getRunCodeStrategyEnum() {
        return RunCodeStrategyEnum.DOCKER;
    }

    /**
     * 子类提供运行时的上下文对象
     */
    protected abstract RunCodeContext buildContext(File userCodeFile, List<String> inputList);

    /**
     * 获取错误相应
     */
    protected abstract String getErrorMessage(String message);


    /**
     * 1.把用户代码保存为文件
     *
     * @param code 用户代码
     * @return 返回创建的文件对象
     */
    protected File createCodeFile(String code) {
        //获取工作目录(在IDE中为项目根目录)
        String userDir = System.getProperty("user.dir");

        //这里用File.separator作为分隔符而不是直接写'\\'是因为在linux和windows上分隔符不一样, 用这个可以自动识别兼容不同系统
        //globalCodePathName: userDir/tempCode
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        //判断全局代码目录是否存在, 没有则新建
        if (GLOBAL_CODE_DIR_NOT_EXIST) {
            synchronized (CodeSandboxTemplate.class) {
                if (GLOBAL_CODE_DIR_NOT_EXIST && !FileUtil.exist(globalCodePathName)) {
                    System.out.println("创建用户代码目录成功");
                    FileUtil.mkdir(globalCodePathName);
                    GLOBAL_CODE_DIR_NOT_EXIST = false;
                }
            }
        }

        //把用户的代码隔离到文件夹中, 命名用UUID
        //userCodePath: userDir/tempCode/UUID/getCodeFileName
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + getCodeFileName();
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 2.编译代码得到class文件
     */
    protected void compileCodeFile(File userCodeFile) throws Exception {
        String compileCmd = getCompileCmd(userCodeFile);
        try {
            //执行cmd命令
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            //获取编译控制台输出
            ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(compileProcess, "编译");
            System.out.println("编译结束, 耗时: " + executeMessage.getTime());
            String errorMessage = executeMessage.getErrorMessage();
            //编译有错误信息
            if (StrUtil.isNotBlank(errorMessage)) {
                throw new Exception(errorMessage);
            }
        } catch (Exception e) {
            //CMD命令执行报错
            throw new Exception(e);
        }
    }

    /**
     * 3.执行代码, 获得执行结果列表, 默认策略为Docker, 子类可以重写更改
     *
     * @param userCodeFile 用户代码文件
     * @param inputList    输入用例
     * @return 执行结果列表
     */
    protected List<ExecuteMessage> runCodeFile(File userCodeFile, List<String> inputList) throws Exception {
        RunStrategy runStrategy = runStrategyManager.getInstance(getRunCodeStrategyEnum().getStrategyName());
        return runStrategy.runCode(buildContext(userCodeFile, inputList));
    }

    /**
     * 4.根据执行结果列表封装输出结果
     *
     * @param executeMessageList 执行结果列表
     * @return 沙箱输出
     */
    protected ExecuteCodeResponse getOutPutResponse(List<ExecuteMessage> executeMessageList) {
        List<String> outputList = new ArrayList<>();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        //程序运行时间和内存取所有用例最大值
        long maxTime = 0L;
        long maxMemory = 0L;

        //遍历输出信息列表
        for (ExecuteMessage executeMessage : executeMessageList) {
            Long time = executeMessage.getTime();
            Long memory = executeMessage.getMemory();
            String message = executeMessage.getMessage();
            String errorMessage = executeMessage.getErrorMessage();
            Integer exitValue = executeMessage.getExitValue();

            //如果运行中有报错, 直接设置信息为异常信息, 设置状态为运行时异常, 退出循环
            if (exitValue != 0 || StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(SandboxResponseStatusEnum.RUNTIME_ERROR.getValue());
                break;
            }
            //将行尾的空格和换行符删除
            outputList.add(message.trim());
            //获取最大时间和内存
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }

        //正常完成
        if (outputList.size() == executeMessageList.size()) {
            //将输出信息拼接后设置到message中
            StringBuilder message = new StringBuilder();
            for (String output : outputList) {
                if (StrUtil.isNotBlank(output)) {
                    message.append(output).append(",");
                }
            }
            if (message.length() == 0) {
                message.append("暂无输出");
            }
            message.deleteCharAt(message.length() - 1);
            executeCodeResponse.setMessage(message.toString());
            //设置状态为AC
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
        executeCodeResponse.setTime(0L);
        executeCodeResponse.setMemory(0L);
        executeCodeResponse.setMessage(getErrorMessage(e.getMessage()));
        //表示代码沙箱错误(编译错误)
        executeCodeResponse.setStatus(SandboxResponseStatusEnum.COMPILE_ERROR.getValue());
        return executeCodeResponse;
    }

    /**
     * 核心模板方法
     *
     * @param executeCodeRequest 代码请求参数
     * @return 沙箱运行结果
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        File userCodeFile = null;
        try {
            //1. 把用户代码存入文件
            userCodeFile = createCodeFile(code);

            //2. 编译用户代码, 得到class文件
            if (enableCompilation()) {
                compileCodeFile(userCodeFile);
            }

            //3. 运行代码, 得到代码用例运行结果
            List<ExecuteMessage> executeMessageList = runCodeFile(userCodeFile, inputList);

            //4.整理输出结果, 将运行结果列表封装成沙箱响应
            return getOutPutResponse(executeMessageList);
        } catch (Exception e) {
            return getErrorResponse(e);
        } finally {
            if (userCodeFile != null) {
                deleteFile(userCodeFile);
            }
        }
    }
}
