package com.tsinghua.edukg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories
public class EdukgApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdukgApplication.class, args);
    }

}
