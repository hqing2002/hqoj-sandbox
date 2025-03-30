package com.hqing.hqojcodesandbox.strategy.impl;

import cn.hutool.core.util.ArrayUtil;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.strategy.RunStrategy;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.utils.ProcessUtils;
import com.hqing.hqojcodesandbox.utils.RegularUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 原生运行策略
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "NativeRunner")
public class NativeRunner implements RunStrategy {
    //定义程序最大运行时长
    private static final long MAX_TIME_LIMIT = 5000L;

    @Override
    public List<ExecuteMessage> runCode(RunCodeContext runCodeContext) throws Exception {
        List<String> inputList = runCodeContext.getInputList();
        String[] runCmd = runCodeContext.getRunCmd();

        //执行代码, 得到输出信息
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String[] inputArgsArray = RegularUtils.parseArguments(inputArgs);
            String[] cmdArray = ArrayUtil.append(runCmd, inputArgsArray);
            try {
                //执行cmd命令
                Process runProcess = Runtime.getRuntime().exec(cmdArray);
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
                executeMessage.setMemory(0L);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
        return executeMessageList;
    }
}
