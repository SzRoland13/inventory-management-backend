package dev.roland.inventory_management_backend.common.annotation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

  private final PasswordValidator validator = new PasswordValidator();

  @Test
  void acceptsPasswordContainingRequiredCharacterClasses() {
    assertTrue(validator.isValid("Correct!42", null));
  }

  @Test
  void rejectsNullShortAndMissingCharacterClasses() {
    assertFalse(validator.isValid(null, null));
    assertFalse(validator.isValid("Ab!2", null));
    assertFalse(validator.isValid("lowercase!2", null));
    assertFalse(validator.isValid("UPPERCASE!2", null));
    assertFalse(validator.isValid("NoDigits!!", null));
    assertFalse(validator.isValid("NoSpecial42", null));
  }

  @Test
  void rejectsRepeatedCharactersAndAscendingOrDescendingSequences() {
    assertFalse(validator.isValid("Abc!1111", null));
    assertFalse(validator.isValid("Abcd!1234", null));
    assertFalse(validator.isValid("Abcd!4321", null));
    assertTrue(validator.isValid("Abc!1357", null));
  }
}
