package com.tsinghua.edukg.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Order(1)
@WebFilter(filterName = "userFilter",urlPatterns = {"/*"})
@Slf4j
public class UserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if(request.getMethod().equals("GET")) {
            String judge = servletRequest.getParameter("test");
            if(StringUtils.isEmpty(judge)) {
                log.info("没有传递用户参数！");
                throw new BusinessException(BusinessExceptionEnum.USER_CHECK_ERROR);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else if(request.getMethod().equals("POST")) {
            RequestWrapper requestWrapper = new RequestWrapper(request);
            String body = IOUtils.toString(requestWrapper.getBody(), "UTF-8");
            JSONObject json = JSON.parseObject(body);
            if(json.get("test") == null) {
                log.info("没有传递用户参数！");
                throw new BusinessException(BusinessExceptionEnum.USER_CHECK_ERROR);
            }
            filterChain.doFilter(requestWrapper, servletResponse);
        }
    }

    @Override
    public void destroy() {
        System.out.println("过滤器被销毁了");
    }
}
