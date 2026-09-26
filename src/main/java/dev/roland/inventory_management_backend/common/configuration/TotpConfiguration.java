package dev.roland.inventory_management_backend.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

/** Provides the secret generator and time source used by TOTP authentication. */
@Configuration
public class TotpConfiguration {

  /**
   * Creates the generator for TOTP shared secrets.
   *
   * @return TOTP secret generator
   */
  @Bean
  public SecretGenerator secretGenerator() {
    return new DefaultSecretGenerator();
  }

  /**
   * Provides the clock used to validate TOTP codes.
   *
   * @return system time source used during TOTP validation
   */
  @Bean
  public dev.samstevens.totp.time.TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }
}
