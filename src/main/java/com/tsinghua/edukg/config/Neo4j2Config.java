package com.tsinghua.edukg.config;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4j2Config {

    @Bean
    @ConfigurationProperties("spring.data.neo4j.domain1")
    public Neo4jProperties neo4jPropertiesDomain1() {
        return new Neo4jProperties();
    }

    @Bean
    @ConfigurationProperties("spring.data.neo4j.domain2")
    public Neo4jProperties neo4jPropertiesDomain2() {
        return new Neo4jProperties();
    }

    @Bean
    public org.neo4j.ogm.config.Configuration ogmConfigurationDomain1() {
        org.neo4j.ogm.config.Configuration c =  neo4jPropertiesDomain1().createConfiguration();
        return c;
    }
    @Bean
    public org.neo4j.ogm.config.Configuration ogmConfigurationDomain2() {
        return neo4jPropertiesDomain2().createConfiguration();
    }

    @Bean
    @Qualifier("sessionFactory1")
    public SessionFactory sessionFactory1() {
        return new SessionFactory(ogmConfigurationDomain1(), "com.tsinghua.edukg.config");
    }

    @Bean
    @Qualifier("sessionFactory")
    public SessionFactory sessionFactory() {
        SessionFactory sessionFactory = new SessionFactory(ogmConfigurationDomain1(), "com.tsinghua.edukg.config");
        return sessionFactory;
    }

    @Bean
    @Qualifier("sessionFactory2")
    public SessionFactory sessionFactory2() {
        return new SessionFactory(ogmConfigurationDomain2(), "com.tsinghua.edukg.config");
    }
}
