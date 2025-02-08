package com.readrealm.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Profile("!test")
public class CachingConfig {

    @Value("${cache.duration.in.days}")
    private int CACHE_DURATION_IN_DAYS;



    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager
                .builder()
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .enableStatistics()
                .build();
    }


    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {

        ObjectMapper redisObjectMapper = objectMapper.copy();

        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        redisObjectMapper.activateDefaultTyping(typeValidator, DefaultTyping.EVERYTHING);


        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(CACHE_DURATION_IN_DAYS))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper)));
    }

}
