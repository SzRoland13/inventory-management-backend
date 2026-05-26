package dev.roland.inventory_management_backend.service.common;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.TimeProvider;

@Service
public class TwoFactorAuthService {

  private final SecretGenerator secretGenerator;
  private final DefaultCodeVerifier verifier;

  private static final int CODE_DIGITS = 6;
  private static final int PERIOD = 30;
  private static final int TOLERANCE_STEPS = 1;

  public TwoFactorAuthService(SecretGenerator secretGenerator, TimeProvider timeProvider) {
    this.secretGenerator = secretGenerator;

    DefaultCodeGenerator codeGenerator =
        new DefaultCodeGenerator(HashingAlgorithm.SHA1, CODE_DIGITS);

    this.verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    this.verifier.setAllowedTimePeriodDiscrepancy(TOLERANCE_STEPS);
  }

  /**
   * Generates a new TOTP shared secret.
   *
   * @return generated secret value
   */
  public String generateSecret() {
    return secretGenerator.generate();
  }

  /**
   * Generates a QR code data URI for enrolling a TOTP authenticator app.
   *
   * @param secret TOTP shared secret
   * @param email user email shown as the QR label
   * @return PNG data URI for the QR code
   */
  public String generateQrCodeImage(String secret, String email) {
    QrData data =
        new QrData.Builder()
            .label(email)
            .issuer("Inventory Management App")
            .secret(secret)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(CODE_DIGITS)
            .period(PERIOD)
            .build();

    QrGenerator generator = new ZxingPngQrGenerator();
    try {
      byte[] imageData = generator.generate(data);
      return getDataUriForImage(imageData, generator.getImageMimeType());
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate QR code", e);
    }
  }

  /**
   * Verifies a user-provided TOTP code against the shared secret.
   *
   * @param secret TOTP shared secret
   * @param code code entered by the user
   * @return true when the code is valid within the configured tolerance window
   */
  public boolean verifyCode(String secret, String code) {
    return verifier.isValidCode(secret, code);
  }
}
