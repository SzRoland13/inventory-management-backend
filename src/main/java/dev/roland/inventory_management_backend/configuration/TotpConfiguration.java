package dev.roland.inventory_management_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

@Configuration
public class TotpConfiguration {

  @Bean
  public SecretGenerator secretGenerator() {
    return new DefaultSecretGenerator();
  }

  @Bean
  public dev.samstevens.totp.time.TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }
}
