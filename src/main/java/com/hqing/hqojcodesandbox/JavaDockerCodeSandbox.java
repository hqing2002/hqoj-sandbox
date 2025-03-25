package com.hqing.hqojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Docker代码沙箱
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class JavaDockerCodeSandbox implements CodeSandbox {
    //将全局用到的字符串(魔法值)都写成字符串常量, 便于维护
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    //定义程序最大运行时长(ms)和内存1G(字节),cpu核数
    private static final long MAX_TIME_LIMIT = 5000L;
    private static final long MAX_MEMORY_LIMIT = 1024 * 1024 * 1024;
    private static final long MAX_CPU_COUNT = 1;


    //判断是否第一次拉取镜像
    private static volatile Boolean FIRST_INIT = true;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

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
            ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(compileProcess, "编译");
            System.out.println("编译结束, 耗时: " + executeMessage.getTime());
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                return getErrorResponse(new Throwable(errorMessage), userCodeFile, userCodeParentPath);
            }
        } catch (Exception e) {
            return getErrorResponse(e, userCodeFile, userCodeParentPath);
        }

        //3. 创建Docker容器, 运行Java代码
        StatsCmd statsCmd = null;
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        final long[] maxMemory = {0L};
        try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
            //镜像名称
            String image = "openjdk:8-alpine";

            //如果是第一次执行则拉取镜像
            if (FIRST_INIT) {
                synchronized (JavaDockerCodeSandbox.class) {
                    if (FIRST_INIT) {
                        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
                        //函数回调
                        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                            @Override
                            public void onNext(PullResponseItem item) {
                                System.out.println("下载镜像" + item.getStatus());
                                super.onNext(item);
                            }
                        };
                        //exec执行, awaitCompletion()阻塞等待异步请求完成
                        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                        FIRST_INIT = false;
                        System.out.println("拉取镜像完成");
                    }
                }
            }

            //配置创建容器的参数
            HostConfig hostConfig = new HostConfig();
            //设置Cpu核数
            hostConfig.withCpuCount(MAX_CPU_COUNT);
            //设置内存大小
            hostConfig.withMemory(MAX_MEMORY_LIMIT);
            //设置文件映射, 设置文件权限为只读
            hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app"), AccessMode.ro));

            //创建容器, 绑定设置, 附加标准输入输出和错误输出, 启动TTY伪终端,使容器支持终端交互
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withTty(true)
                    .exec();
            //获取创建出的容器id
            String containerId = createContainerResponse.getId();
            //启动容器
            dockerClient.startContainerCmd(containerId).exec();

            //获取占用内存
            statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback.Adapter<Statistics> statsResultCallback = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    Long memory = statistics.getMemoryStats().getUsage();
                    if (memory != null && memory > maxMemory[0]) {
                        maxMemory[0] = memory;
                        System.out.println("[内存监控] 更新峰值: " + NumberUtil.div(maxMemory[0], 1024.0 * 1024.0, 1) + " MB");
                    }
                }
            };
            //开启内存监控
            statsCmd.exec(statsResultCallback);
            //执行命令docker exec containerName java -cp /app Main 1 2
            for (String inputArgs : inputList) {
                //设置命令参数, 把命令按照空格拆分，作为一个数组传递
                String[] inputArgsArray = inputArgs.split(" ");
                String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
                //创建Cmd执行对象, 开启输入输出流
                ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd(cmdArray)
                        .withAttachStderr(true)
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .exec();
                System.out.println("执行命令: " + Arrays.toString(cmdArray));
                String execId = execCreateCmdResponse.getId();

                //收集输出信息
                ExecuteMessage executeMessage = new ExecuteMessage();
                final StringBuilder messageBuilder = new StringBuilder();
                final StringBuilder errMessageBuilder = new StringBuilder();
                final int[] exitValue = {0};
                //启动Cmd执行对象, 编写回调函数
                ResultCallback.Adapter<Frame> execStartResultCallback = new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        //判断输出流类型
                        StreamType streamType = frame.getStreamType();
                        String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        if (StreamType.STDOUT.equals(streamType)) {
                            messageBuilder.append(payload);
                            System.out.println("输出正常结果: " + payload);
                        } else if (StreamType.STDERR.equals(streamType)) {
                            //exitValue设置为出错
                            exitValue[0] = 1;
                            errMessageBuilder.append(payload);
                            System.err.println("输出错误结果: " + payload);
                        } else {
                            System.out.println("其他错误类型: " + new String(frame.getPayload()));
                        }
                    }
                };
                //开启定时器统计时间
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                //执行命令并阻塞等待结束
                dockerClient.execStartCmd(execId).exec(execStartResultCallback).awaitCompletion();
                //关闭定时器
                stopWatch.stop();
                //填充本次运行的信息
                executeMessage.setMessage(messageBuilder.toString());
                executeMessage.setErrorMessage(errMessageBuilder.toString());
                executeMessage.setExitValue(exitValue[0]);
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessageList.add(executeMessage);
            }
            //先关闭回调适配器（停止处理数据）, 等待延迟后关闭内存监控
            statsResultCallback.close();
            Thread.sleep(100);
            statsCmd.close();

            //删除容器
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    System.err.println("删除容器失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("容器执行出现异常" + e.getMessage());
            return getErrorResponse(e, userCodeFile, userCodeParentPath);
        } finally {
            if (statsCmd != null) {
                statsCmd.close();
            }
        }

        //4.整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();

        //取最大值判断是否超时
        long maxTime = 0L;

        for (ExecuteMessage executeMessage : executeMessageList) {
            Long time = executeMessage.getTime();
            String message = executeMessage.getMessage();
            String errorMessage = executeMessage.getErrorMessage();
            Integer exitValue = executeMessage.getExitValue();

            //如果运行中有报错
            if (exitValue != 0 || StrUtil.isNotBlank(errorMessage)) {
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
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setTime(maxTime);
        executeCodeResponse.setMemory(maxMemory[0]);

        //5. 文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }

        System.out.println("docker程序执行结束, 获取到的结果为: " + executeMessageList);
        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e, File userCodeFile, String userCodeParentPath) {
        //文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误(编译错误)
        executeCodeResponse.setStatus(2);
        return executeCodeResponse;
    }
}
