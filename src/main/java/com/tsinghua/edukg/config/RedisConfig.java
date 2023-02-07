package com.tsinghua.edukg.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import javax.annotation.Resource;
import java.time.Duration;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisConfig {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;

    @Value("${spring.redis.prefix}")
    private String prefix;

    @Value("${spring.redis.opengate}")
    private Integer openGate;

    /*@Bean
    public RedisConnectionFactory redisConnectionFactory() {
        Map<String, Object> source = new HashMap<>();
        source.put("spring.redis.cluster.nodes", ipNodes);
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
                new MapPropertySource("RedisClusterConfiguration", source));

        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder =
                LettuceClientConfiguration.builder();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisClusterConfiguration,
                lettuceClientConfigurationBuilder.build());
        return factory;
    }*/

    // 不设置成Bean
    public ObjectMapper getObjMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return om;
    }

    public Integer getOpenGate() {
        return openGate;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);

        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        serializer.setObjectMapper(getObjMapper());

        template.setValueSerializer(serializer);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new PrefixRedisSerializer(prefix));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        serializer.setObjectMapper(getObjMapper());
        RedisCacheConfiguration sentFbCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24)).disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new PrefixRedisSerializer(prefix))).serializeValuesWith(RedisSerializationContext.
                        SerializationPair.fromSerializer(serializer));
        RedisCacheManager cacheManager = RedisCacheManager
                .builder(connectionFactory).cacheDefaults(sentFbCacheConfig).build();
        return cacheManager;
    }

    static class PrefixRedisSerializer implements RedisSerializer<String> {
        private final RedisSerializer<String> delegate = RedisSerializer.string();

        private final String prefix;

        public PrefixRedisSerializer(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public byte[] serialize(String s) throws SerializationException {
            return delegate.serialize(prefix + s);
        }

        @Override
        public String deserialize(byte[] bytes) throws SerializationException {
            return delegate.deserialize(bytes);
        }
    }
}


