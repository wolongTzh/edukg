package com.tsinghua.edukg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig  implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /data/**是静态映射， file:/data/是文件在服务器的路径
        registry.addResourceHandler("/data/**")
                .addResourceLocations("file:/data/");
        // /data1/**是静态映射， file:/data1/是文件在服务器的路径
        registry.addResourceHandler("/data1/**")
                .addResourceLocations("file:/data1/");
    }
}
