package utils;

public final class InputValidator {
    private InputValidator() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
    }

    public static void requireEmail(String email) {
        requireNonBlank(email, "Email");
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }
}
