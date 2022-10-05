package com.tsinghua.edukg.config;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories("com.demo.devops.itca.neo4j")
public class Neo4jConfig {

    @Autowired
    SessionFactory sessionFactory;

    @Bean
    @Qualifier("neo4jSession")
    //spingboot.data里需要transactionManager名字的实例
    public Session createSession() {
        return sessionFactory.openSession();
    }
}
