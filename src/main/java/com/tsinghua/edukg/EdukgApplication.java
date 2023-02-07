package com.tsinghua.edukg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableNeo4jRepositories
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableFeignClients
public class EdukgApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdukgApplication.class, args);
    }

}
