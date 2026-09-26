package dev.roland.inventory_management_backend.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** Configures the Redis connection and serializers used by short-lived login sessions. */
@Configuration
public class RedisConfiguration {

  @Value("${spring.data.redis.host}")
  private String RedisHost;

  @Value("${spring.data.redis.port}")
  private int RedisPort;

  /**
   * Creates the connection used by Redis clients.
   *
   * @return Redis connection factory
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(RedisHost, RedisPort);
  }

  /**
   * Creates a Redis template for application session data.
   *
   * @param connectionFactory connection used by the template
   * @return template configured with string serializers
   */
  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    return template;
  }
}
