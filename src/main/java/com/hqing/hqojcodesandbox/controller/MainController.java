package com.hqing.hqojcodesandbox.controller;

import cn.hutool.core.io.resource.ResourceUtil;
import com.hqing.hqojcodesandbox.impl.CodeSandbox;
import com.hqing.hqojcodesandbox.impl.java.JavaDockerCodeSandbox;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@RestController("/")
public class MainController {

    @GetMapping("/health/{id1}/{id2}/{filePath}")
    public String healthCheck(@PathVariable("id1") String id1,
                              @PathVariable("id2") String id2,
                              @PathVariable("filePath") String filePath) {
        CodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();

        //读取resource目录下的main.java用于测试
        String path = "testCode" + File.separator + File.separator + filePath + File.separator + "Main.java";
        String code = ResourceUtil.readStr(path, StandardCharsets.UTF_8);
        executeCodeRequest.setInputList(Collections.singletonList(id1 + " " + id2));
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        return executeCodeResponse.toString();
    }
}
