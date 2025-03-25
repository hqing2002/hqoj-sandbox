package com.hqing.hqojcodesandbox.utils;

import com.hqing.hqojcodesandbox.model.ProcessMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * 进程工具类
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class ProcessUtils {
    /**
     * 获取进程的控制台输出信息
     *
     * @param runProcess
     * @param operateName
     */
    public static ProcessMessage getProcessMessage(Process runProcess, String operateName) {
        ProcessMessage processMessage = new ProcessMessage();
        try (BufferedReader stdReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()))) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int exitValue = runProcess.waitFor();
            processMessage.setExitValue(exitValue);

            // 读取标准输出
            StringBuilder stdOutput = new StringBuilder();
            String line;
            while ((line = stdReader.readLine()) != null) {
                stdOutput.append(line);
            }
            processMessage.setMessage(stdOutput.toString());

            // 读取错误输出
            StringBuilder errOutput = new StringBuilder();
            while ((line = errReader.readLine()) != null) {
                errOutput.append(line).append("\n");
            }
            processMessage.setErrorMessage(errOutput.toString());

            if (exitValue == 0) {
                System.out.println(operateName + "成功");
            } else {
                System.out.println(operateName + "失败, 错误码: " + exitValue);
            }
            stopWatch.stop();
            processMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new RuntimeException(e);
        }
        return processMessage;
    }

    /**
     * 执行交互式进程, 并获取信息, 注意调用方需要自己拼接参数
     *
     * @param runProcess
     * @param input
     * @return
     */
    public static ProcessMessage runInteractProcessAndGetMessage(Process runProcess, String input) {
        ProcessMessage processMessage = new ProcessMessage();
        try (OutputStream outputStream = runProcess.getOutputStream();
             InputStream inputStream = runProcess.getInputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            //向控制台输入程序
            outputStreamWriter.write(input);
            //相当于按下回车，执行发送
            outputStreamWriter.flush();
            //分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            //逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            processMessage.setMessage(compileOutputStringBuilder.toString());
            //记得资源释放，否则会卡死
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (IOException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new RuntimeException(e);
        }
        return processMessage;
    }
}
