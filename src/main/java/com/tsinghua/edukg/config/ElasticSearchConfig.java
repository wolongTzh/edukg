package com.tsinghua.edukg.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${spring.es.address}")
    String address;

    @Value("${spring.es.port}")
    Integer port;

    @Value("${spring.es.scheme}")
    String scheme;

    @Value("${spring.es.username}")
    String username;

    @Value("${spring.es.password}")
    String password;

    @Value("${spring.es.index.examsource}")
    String examSourceIndex;

    @Value("${spring.es.index.textbook}")
    String textBookIndex;

    @Value("${spring.es.index.irqa}")
    String irqaIndex;

    @Bean
    public ElasticsearchClient esRestClientWithCred(){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // 配置连接ES的用户名和密码，如果没有用户名和密码可以不加这一行
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(address, port, scheme))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestClient restClient = restClientBuilder.build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return client;
    }

    public String getExamSourceIndex() {
        return examSourceIndex;
    }

    public String getTextBookIndex() {
        return textBookIndex;
    }

    public String getIrqaIndex() {
        return irqaIndex;
    }
}
