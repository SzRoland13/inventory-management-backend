package dev.roland.inventory_management_backend.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class TwoFactorAuthService {

    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final DefaultCodeVerifier verifier;

    private static final int CODE_DIGITS = 6;
    private static final int PERIOD = 30;
    private static final int TOLERANCE_STEPS = 1;

    public TwoFactorAuthService(SecretGenerator secretGenerator, QrDataFactory qrDataFactory) {
        this.secretGenerator = secretGenerator;
        this.qrDataFactory = qrDataFactory;

        TimeProvider timeProvider = new SystemTimeProvider();
        DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, CODE_DIGITS);

        this.verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        this.verifier.setAllowedTimePeriodDiscrepancy(TOLERANCE_STEPS);
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeImage(String secret, String email) {
        QrData data = qrDataFactory.newBuilder()
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

    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
