package dev.roland.inventory_management_backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Applies the same practical SMTP address syntax and limits as the frontend schema. */
public class EmailAddressValidator implements ConstraintValidator<EmailAddress, String> {
  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return value == null || value.isEmpty() || hasValidEmailShape(value);
  }

  private boolean hasValidEmailShape(final String value) {
    final int atIndex = value.indexOf('@');
    final boolean validAddressLength = value.length() <= 254;
    final boolean exactlyOneAt = atIndex > 0 && atIndex == value.lastIndexOf('@');
    final boolean validLocalPart =
        exactlyOneAt && atIndex <= 64 && isValidLocalPart(value.substring(0, atIndex));
    final boolean validDomain = exactlyOneAt && isValidDomain(value.substring(atIndex + 1));
    return validAddressLength && validLocalPart && validDomain;
  }

  private boolean isValidLocalPart(final String localPart) {
    boolean valid = !localPart.startsWith(".") && !localPart.endsWith(".");
    for (int index = 0; valid && index < localPart.length(); index++) {
      final char character = localPart.charAt(index);
      valid = isAsciiLetterOrDigit(character) || isAllowedLocalSymbol(character);
      if (character == '.' && index > 0) {
        valid = localPart.charAt(index - 1) != '.';
      }
    }
    return valid;
  }

  private boolean isValidDomain(final String domain) {
    boolean valid = !domain.isEmpty();
    boolean foundDot = false;
    int labelStart = 0;
    int finalLabelLength = 0;
    for (int index = 0; valid && index <= domain.length(); index++) {
      if (index == domain.length() || domain.charAt(index) == '.') {
        final int labelLength = index - labelStart;
        valid = isValidDomainLabel(domain, labelStart, index, labelLength);
        finalLabelLength = labelLength;
        labelStart = index + 1;
        foundDot = foundDot || index < domain.length();
      }
    }
    final int finalLabelStart = domain.length() - finalLabelLength;
    return valid && foundDot && finalLabelLength >= 2 && isAsciiAlphaLabel(domain, finalLabelStart);
  }

  private boolean isValidDomainLabel(
      final String domain, final int labelStart, final int labelEnd, final int labelLength) {
    boolean valid =
        labelLength > 0
            && labelLength <= 63
            && isAsciiLetterOrDigit(domain.charAt(labelStart))
            && isAsciiLetterOrDigit(domain.charAt(labelEnd - 1));
    for (int index = labelStart; valid && index < labelEnd; index++) {
      final char character = domain.charAt(index);
      valid = isAsciiLetterOrDigit(character) || character == '-';
    }
    return valid;
  }

  private boolean isAsciiAlphaLabel(final String domain, final int start) {
    boolean valid = true;
    for (int index = start; valid && index < domain.length(); index++) {
      final char character = domain.charAt(index);
      valid = (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
    }
    return valid;
  }

  private boolean isAsciiLetterOrDigit(final char character) {
    return (character >= 'A' && character <= 'Z')
        || (character >= 'a' && character <= 'z')
        || (character >= '0' && character <= '9');
  }

  private boolean isAllowedLocalSymbol(final char character) {
    return "!#$%&'*+/=?^_`{|}~-".indexOf(character) >= 0;
  }
}
