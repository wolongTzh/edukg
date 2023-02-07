package com.tsinghua.edukg.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean registFilter(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new UserFilter());
        registrationBean.addUrlPatterns("/api/editor/*");
        registrationBean.addUrlPatterns("/api/statistic/*");
        registrationBean.setName("userFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
