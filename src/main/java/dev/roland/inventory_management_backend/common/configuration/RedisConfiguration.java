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
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  /**
   * Creates the connection used by Redis clients.
   *
   * @return Redis connection factory
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(redisHost, redisPort);
  }

  /**
   * Creates a Redis template for application session data.
   *
   * @param connectionFactory connection used by the template
   * @return template configured with string serializers
   */
  @Bean
  public RedisTemplate<String, String> redisTemplate(
      final RedisConnectionFactory connectionFactory) {
    final RedisTemplate<String, String> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    return template;
  }
}
