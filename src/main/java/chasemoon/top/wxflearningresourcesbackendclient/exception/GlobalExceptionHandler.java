package chasemoon.top.wxflearningresourcesbackendclient.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import chasemoon.top.wxflearningresourcesbackendclient.common.Result;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("未捕获的异常", e);
        return Result.error(500, "系统内部错误");
    }

    @ExceptionHandler(ServiceException.class)
    public Result<Object> handleServiceException(ServiceException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
} 