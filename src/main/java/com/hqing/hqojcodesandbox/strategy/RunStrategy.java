package com.hqing.hqojcodesandbox.strategy;

import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import com.hqing.hqojcodesandbox.strategy.model.RunCodeContext;

import java.util.List;

/**
 * 沙箱代码运行策略
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public interface RunStrategy {
    List<ExecuteMessage> runCode(RunCodeContext runCodeContext) throws Exception;
}
