package dev.roland.inventory_management_backend.common.mail;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.exception.MailException;
import dev.roland.inventory_management_backend.common.message.MailMessageKey;

/** Finds the factory for a mailable and composes its message. */
@Component
public class MailComposer {

  private final Map<MailType, MailFactory> factories;
  private final boolean invalidFactoryConfiguration;

  /**
   * Builds a private factory lookup from the registered mail factories.
   *
   * @param mailFactories factories available for composing mail types
   */
  public MailComposer(final List<MailFactory> mailFactories) {
    final Map<MailType, MailFactory> byType = new EnumMap<>(MailType.class);
    boolean hasDuplicateTypes = false;
    for (final MailFactory factory : mailFactories) {
      if (byType.put(factory.getType(), factory) != null) {
        hasDuplicateTypes = true;
      }
    }
    this.factories = byType;
    this.invalidFactoryConfiguration = hasDuplicateTypes;
  }

  /**
   * Validates and indexes the available factories after dependency injection.
   *
   * @throws MailException if multiple factories are registered for one type
   */
  @PostConstruct
  public void initializeFactories() {
    if (invalidFactoryConfiguration) {
      throw new MailException(MailMessageKey.FACTORY_CONFIGURATION_INVALID);
    }
  }

  /**
   * Composes a mailable using the factory registered for its type.
   *
   * @param mailable email request to compose
   * @return fully composed email
   * @throws MailException if no factory is registered for the mailable type
   */
  public ComposedMail compose(final Mailable mailable) {
    final MailFactory factory = factories.get(mailable.type());

    if (factory == null) {
      throw new MailException(MailMessageKey.FACTORY_NOT_FOUND);
    }

    return factory.compose(mailable);
  }
}
