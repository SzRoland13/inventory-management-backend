package dev.roland.inventory_management_backend.common.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the application clock used by time-sensitive services. */
@Configuration
public class TimeConfiguration {

  /**
   * Provides the application clock.
   *
   * @return clock result
   */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
