package com.hqing.hqojcodesandbox.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.hqing.hqojcodesandbox.common.BaseResponse;
import com.hqing.hqojcodesandbox.common.ErrorCode;
import com.hqing.hqojcodesandbox.common.ResultUtils;
import com.hqing.hqojcodesandbox.exception.BusinessException;
import com.hqing.hqojcodesandbox.impl.CodeSandbox;
import com.hqing.hqojcodesandbox.impl.CodeSandboxFactory;
import com.hqing.hqojcodesandbox.model.ExecuteCodeRequest;
import com.hqing.hqojcodesandbox.model.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 代码沙箱Controller
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@RestController("/")
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
        if (executeCodeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String secret = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if(StrUtil.isBlank(secret) || !secret.equals(AUTH_REQUEST_SECRET)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        if (StringUtils.isAnyBlank(code, language) || CollectionUtil.isEmpty(inputList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CodeSandbox codeSandbox = factory.newInstance(language);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        return ResultUtils.success(executeCodeResponse);
    }
}
