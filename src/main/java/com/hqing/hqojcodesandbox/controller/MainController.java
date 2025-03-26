package com.hqing.hqojcodesandbox.controller;

import cn.hutool.core.collection.CollectionUtil;
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
import java.util.List;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@RestController("/")
public class MainController {
    @Resource
    private CodeSandboxFactory factory;

    @GetMapping("/health")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }

    @PostMapping("/executeCode")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        if (executeCodeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
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
