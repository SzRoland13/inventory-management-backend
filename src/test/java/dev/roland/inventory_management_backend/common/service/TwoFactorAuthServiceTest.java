package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.TimeProvider;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {

  @Mock private SecretGenerator secretGenerator;
  @Mock private TimeProvider timeProvider;

  private TwoFactorAuthService service;

  @BeforeEach
  void setUp() {
    service = new TwoFactorAuthService(secretGenerator, timeProvider);
  }

  @Test
  void generatesSharedSecretAndAuthenticatorQrDataUri() {
    when(secretGenerator.generate()).thenReturn("JBSWY3DPEHPK3PXP");

    assertEquals("JBSWY3DPEHPK3PXP", service.generateSecret());
    assertTrue(
        service
            .generateQrCodeImage("JBSWY3DPEHPK3PXP", "alice@example.com")
            .startsWith("data:image/png;base64,"));
  }

  @Test
  void rejectsAnInvalidAuthenticatorCode() {
    when(timeProvider.getTime()).thenReturn(1_700_000_000L);

    assertFalse(service.verifyCode("JBSWY3DPEHPK3PXP", "abcdef"));
  }
}
