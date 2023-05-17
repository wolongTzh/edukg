package com.tsinghua.edukg.config;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories("com.demo.devops.itca.neo4j")
public class Neo4jConfig {

    @Autowired
    @Qualifier("sessionFactory1")
    SessionFactory sessionFactory1;

    @Autowired
    @Qualifier("sessionFactory2")
    SessionFactory sessionFactory2;

    @Bean
    @Qualifier("neo4jSession")
    //spingboot.data里需要transactionManager名字的实例
    public Session createSession() {
        return sessionFactory1.openSession();
    }
    @Bean
    @Qualifier("neo4jSession2")
    //spingboot.data里需要transactionManager名字的实例
    public Session createSession2() {
        return sessionFactory2.openSession();
    }
}
