package com.tsinghua.edukg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        //设置中文乱码问题方式一
//        restTemplate.getMessageConverters().add(1,new StringHttpMessageConverter(Charset.forName("UTF-8")));
        // 设置中文乱码问题方式二
        // restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 支持中文编码
        return restTemplate;
    }
}
