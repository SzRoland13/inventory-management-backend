package dev.roland.inventory_management_backend.common.configuration;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/** Resolves request locales from the frontend's locale cookie. */
@Configuration
public class LocaleConfiguration {

  public static final String LOCALE_COOKIE_NAME = "NEXT_LOCALE";

  /**
   * Creates the locale resolver used by Spring MVC for incoming requests.
   *
   * @return cookie-based locale resolver
   */
  @Bean
  public LocaleResolver localeResolver() {
    final CookieLocaleResolver localeResolver = new CookieLocaleResolver(LOCALE_COOKIE_NAME);
    localeResolver.setDefaultLocale(Locale.ENGLISH);
    return localeResolver;
  }
}
