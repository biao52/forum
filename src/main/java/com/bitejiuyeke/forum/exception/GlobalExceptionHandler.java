package com.bitejiuyeke.forum.exception;

import com.bitejiuyeke.forum.common.AppResult;
import com.bitejiuyeke.forum.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 *
 * @Author 比特就业课
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ApplicationException.class)
    public AppResult applicationExceptionHandler (ApplicationException e) {
        // 打印异常信息
        e.printStackTrace(); // 上生产之前要删除
        // 打印日志
        log.error(e.getMessage());
        if (e.getErrorResult() != null) {
            return e.getErrorResult();
        }
        // 非空校验
        if (e.getMessage() == null || e.getMessage().equals("")) {
            return AppResult.failed(ResultCode.ERROR_SERVICES);
        }
        // 返回具体的异常信息
        return AppResult.failed(e.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(Exception.class)
    public AppResult exceptionHandler (Exception e) {
        // 打印异常信息
        e.printStackTrace();
        // 打印日志
        log.error(e.getMessage());
        // 非空校验
        if (e.getMessage() == null || e.getMessage().equals("")) {
            return AppResult.failed(ResultCode.ERROR_SERVICES);
        }
        // 返回异常信息
        return AppResult.failed(e.getMessage());
    }
}
