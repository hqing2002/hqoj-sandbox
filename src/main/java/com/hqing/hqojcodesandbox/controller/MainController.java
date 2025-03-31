package com.hqing.hqojcodesandbox.controller;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.hqing.hqojcodesandbox.common.BaseResponse;
import com.hqing.hqojcodesandbox.common.ErrorCode;
import com.hqing.hqojcodesandbox.common.ResultUtils;
import com.hqing.hqojcodesandbox.core.CodeSandbox;
import com.hqing.hqojcodesandbox.core.CodeSandboxFactory;
import com.hqing.hqojcodesandbox.exception.BusinessException;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代码沙箱Controller
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@RestController("/")
@Slf4j
public class MainController {
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private CodeSandboxFactory factory;

    @GetMapping("/health")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }

    @PostMapping("/executeCode")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest httpServletRequest) {
        //传入参数异常
        if (executeCodeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //权限校验
        String secret = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if (StrUtil.isBlank(secret) || !secret.equals(AUTH_REQUEST_SECRET)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //参数校验
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        //todo 测试, 设置用Java原生
        language = "java-native";
        if (StringUtils.isAnyBlank(code, language)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果用户的代码不需要输入, 这里可以设置一个空字符串列表保证代码可以执行一次
        List<String> inputList = executeCodeRequest.getInputList();
        if (inputList == null || inputList.isEmpty()) {
            inputList = new ArrayList<>(Collections.singletonList(""));
        }
        for (int i = 0; i < inputList.size(); i++) {
            if (StrUtil.isBlank(inputList.get(i))) {
                inputList.set(i, "");
            }
        }
        executeCodeRequest.setInputList(inputList);
        //日志打印
        String separators = "###############################################################";
        log.info("\n{}\n用户提交代码, 语言:{}\n{}", separators, language, code);
        //沙箱工厂调用沙箱实现类
        CodeSandbox codeSandbox = factory.newInstance(language);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        //截取前几个输出元素
        List<String> outputList = ListUtil.sub(executeCodeResponse.getOutputList(), 0, 10);
        String message = executeCodeResponse.getMessage();
        Integer status = executeCodeResponse.getStatus();
        Long time = executeCodeResponse.getTime();
        Long memory = executeCodeResponse.getMemory();
        log.info("\n调用完成, 前十个输出结果:{}\n信息: {}\n调用状态: {}\n时间消耗: {}\n内存消耗: {}\n{}",
                outputList, message, status, time, memory, separators);
        return ResultUtils.success(executeCodeResponse);
    }
}
