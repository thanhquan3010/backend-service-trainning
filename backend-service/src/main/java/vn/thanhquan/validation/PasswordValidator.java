package vn.thanhquan.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for @ValidPassword annotation.
 * Checks password against security requirements.
 *
 * @author Backend Team
 * @version 1.0
 * @since 2025-10-21
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            setCustomMessage(context, "Password must be at least " + MIN_LENGTH + " characters long");
            return false;
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            setCustomMessage(context, "Password must contain at least one uppercase letter");
            return false;
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            setCustomMessage(context, "Password must contain at least one lowercase letter");
            return false;
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            setCustomMessage(context, "Password must contain at least one digit");
            return false;
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            setCustomMessage(context, "Password must contain at least one special character (!@#$%^&*(),.?\":{}|<>)");
            return false;
        }

        return true;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

