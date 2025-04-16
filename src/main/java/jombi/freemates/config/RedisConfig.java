package jombi.freemates.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories
public class RedisConfig {
  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {

    return new LettuceConnectionFactory(host, port);
  }

  @Bean(name ="objectRedisTemplate")
  public RedisTemplate<String, Object> objectRedisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    // 키는 String으로 직렬화
    template.setKeySerializer(new StringRedisSerializer());
    // 값은 JSON으로 직렬화
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    // 해시 키/값도 동일하게 설정
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }








}

