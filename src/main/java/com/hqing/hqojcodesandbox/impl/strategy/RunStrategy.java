package com.hqing.hqojcodesandbox.impl.strategy;

import com.hqing.hqojcodesandbox.impl.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.impl.strategy.model.RunCodeResult;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 沙箱代码运行策略
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public interface RunStrategy {
    List<ExecuteMessage> runCode(RunCodeContext runCodeContext) throws Exception;
}
