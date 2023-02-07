package com.tsinghua.edukg.api.config;

import com.tsinghua.edukg.api.utils.MyJackson2HttpMessageConverter;
import feign.Logger;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:tangtongda@gmail.com">Tino.Tang</a>
 * @version ${project.version} - 2021/11/29
 */
@Configuration
public class OpenFeignLogConfig {

    @Bean
    public Logger.Level feignLoggerLeave() {
        return Logger.Level.FULL;
    }

    @Bean
    public Decoder feignDecoder() {
        MyJackson2HttpMessageConverter converter = new MyJackson2HttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(converter);
        return new SpringDecoder(objectFactory);
    }
}
