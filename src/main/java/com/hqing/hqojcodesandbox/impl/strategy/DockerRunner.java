package com.hqing.hqojcodesandbox.impl.strategy;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.hqing.hqojcodesandbox.impl.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.utils.RegularUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Docker运行策略
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "DockerRunner")
public class DockerRunner implements RunStrategy {
    private static final long MAX_TIME_LIMIT = 5000L;
    private static final long MAX_MEMORY_LIMIT = 256 * 1024 * 1024;
    //判断是否第一次拉取镜像
    private static volatile Boolean FIRST_INIT = true;

    @Override
    public List<ExecuteMessage> runCode(RunCodeContext runCodeContext) throws Exception {
        File userCodeFile = runCodeContext.getUserCodeFile();
        List<String> inputList = runCodeContext.getInputList();
        String dockerImage = runCodeContext.getDockerImage();
        String[] runCmd = runCodeContext.getRunCmd();
        AccessMode accessMode = runCodeContext.getAccessMode();

        //Docker容器状态, 监控内存
        StatsCmd statsCmd = null;
        final long[] maxMemory = {0L};

        //获取代码文件目录
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        //连接Docker
        String containerId = null;
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        try {
            //如果是第一次执行则拉取镜像
            if (FIRST_INIT) {
                synchronized (DockerRunner.class) {
                    if (FIRST_INIT) {
                        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(dockerImage);
                        //函数回调
                        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                            @Override
                            public void onNext(PullResponseItem item) {
                                super.onNext(item);
                                System.out.println("下载镜像" + item.getStatus());
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
            //设置内存大小
            hostConfig.withMemory(MAX_MEMORY_LIMIT);
            //禁止内存超限
            hostConfig.withMemorySwap(0L);
            //限制用户写入root目录
            hostConfig.withReadonlyRootfs(true);
            //设置文件映射, 设置文件权限为只读
            hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app"), accessMode));

            //创建容器, 绑定设置, 附加标准输入输出和错误输出, 启动TTY伪终端,使容器支持终端交互
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(dockerImage);
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withNetworkDisabled(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withTty(true)
                    .exec();
            //获取创建出的容器id
            containerId = createContainerResponse.getId();
            //启动容器
            dockerClient.startContainerCmd(containerId).exec();

            //创建容器状态监控, 编写回调函数, 回调函数会按照固定时间执行
            statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback.Adapter<Statistics> statsResultCallback = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    super.onNext(statistics);
                    //获取容器当前内存占用
                    Long memory = statistics.getMemoryStats().getUsage();
                    if (memory != null && memory > maxMemory[0]) {
                        maxMemory[0] = memory;
                        System.out.println("[内存监控] 更新峰值: " + NumberUtil.div(maxMemory[0], 1024.0 * 1024.0, 1) + " MB");
                    }
                }
            };
            //开启内存监控
            statsCmd.exec(statsResultCallback);
            //Docker容器中的TTY执行命令
            for (String inputArgs : inputList) {
                //设置命令参数, 把命令按照空格拆分，作为一个数组传递
                String[] inputArgsArray = RegularUtils.parseArguments(inputArgs);
                String[] cmdArray = ArrayUtil.append(runCmd, inputArgsArray);
                System.out.println("执行命令: " + Arrays.toString(cmdArray));
                //创建Cmd执行对象, 开启输入输出流
                ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd(cmdArray)
                        .withAttachStderr(true)
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .exec();
                String execId = execCreateCmdResponse.getId();

                //收集输出信息
                ExecuteMessage executeMessage = new ExecuteMessage();
                final StringBuilder messageBuilder = new StringBuilder();
                final StringBuilder errMessageBuilder = new StringBuilder();
                final int[] exitValue = {0};
                //启动容器Cmd执行对象, 编写回调函数
                ResultCallback.Adapter<Frame> execStartResultCallback = new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        super.onNext(frame);
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
                //执行cmd命令并阻塞等待结束, 同时限定该条命令最大执行时间, 超时返回false
                boolean notTimeout = dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(MAX_TIME_LIMIT, TimeUnit.MILLISECONDS);
                //关闭定时器
                stopWatch.stop();
                long time = stopWatch.getLastTaskTimeMillis();

                //如果程序超出我们设定的最大运行时间, 则直接将时间给一个随机数
                if (!notTimeout) {
                    time = ThreadLocalRandom.current().nextInt(2_000_000, 4_000_001);
                }
                //填充本次运行的信息
                executeMessage.setMessage(messageBuilder.toString());
                executeMessage.setErrorMessage(errMessageBuilder.toString());
                executeMessage.setExitValue(exitValue[0]);
                executeMessage.setTime(time);
                executeMessage.setMemory(maxMemory[0]);
                executeMessageList.add(executeMessage);
            }
            //先关闭回调适配器（停止处理数据）, 等待延迟后关闭内存监控
            statsResultCallback.close();
            Thread.sleep(100);
            statsCmd.close();
            return executeMessageList;
        } catch (Exception e) {
            System.out.println("容器执行出现异常" + e.getMessage());
            throw new Exception(e);
        } finally {
            if (statsCmd != null) {
                statsCmd.close();
            }
            //删除容器
            if (containerId != null) {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            }
            dockerClient.close();
        }
    }
}
