package com.hqing.hqojcodesandbox.impl.strategy;

import com.hqing.hqojcodesandbox.impl.strategy.model.RunCodeContext;
import com.hqing.hqojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 原生运行策略
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Component(value = "NativeRunner")
public class NativeRunner implements RunStrategy {
    @Override
    public List<ExecuteMessage> runCode(RunCodeContext runCodeContext) {
        System.out.println("Native执行运行代码操作");
        return null;
    }
}
