package com.hqing.hqojcodesandbox.strategy.model;

import com.github.dockerjava.api.model.AccessMode;
import lombok.Data;

import java.io.File;
import java.util.List;

/**
 * 代码执行策略上下文对象
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@Data
public class RunCodeContext {
    private File userCodeFile;
    private List<String> inputList;
    private String dockerImage;
    private String[] runCmd;
    private AccessMode accessMode;
}
