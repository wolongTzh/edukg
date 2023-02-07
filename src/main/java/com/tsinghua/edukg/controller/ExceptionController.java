package com.tsinghua.edukg.controller;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.constant.WebConstant;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 全局异常处理
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@RestControllerAdvice
@Slf4j
public class ExceptionController {

    /**
     * 业务异常
     * @param e
     * @param hsp
     * @return
     */
    @ExceptionHandler(value = {BusinessException.class})
    public Object businessException(BusinessException e, HttpServletRequest hsp) {
        Map<String, String[]> parameterMap = hsp.getParameterMap();
        log.error("businessException params={}", JSON.toJSONString(parameterMap), e);
        return WebUtil.errResult(e.getCode(), e.getMessage());
    }

    /**
     * 参数类型检测异常
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Object handlerValidParamError(BindException ex, HttpServletRequest hsp) {
        Map<String, String[]> parameterMap = hsp.getParameterMap();
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error(msg);
        log.error("BindException params={}", JSON.toJSONString(parameterMap), ex);
        String reason = "Internal Server Error";
        return WebUtil.errResult(WebConstant.RESULT_SERVER_ERROR_CODE, reason);
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object exception(Exception exception, WebRequest request) {
        // 有些异常不需要报警
        if (exception instanceof MissingServletRequestParameterException) {
            log.error("ServerException# Request={}", request.getDescription(true), exception);
        } else {
            log.error("ServerException! params={}", request.getDescription(true), exception);
        }
        String reason = "Internal Server Error";
        return WebUtil.errResult(WebConstant.RESULT_SERVER_ERROR_CODE, reason);
    }

    @ExceptionHandler(value = {NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object noMapping(WebRequest request) {
        log.warn("@@ServerNoMapping Request={}", request.getDescription(true));
        return WebUtil.errResult(WebConstant.RESULT_NOFOUND_ERROR_CODE, "Not Found");
    }
}
