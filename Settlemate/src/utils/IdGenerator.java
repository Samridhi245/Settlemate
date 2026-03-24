package utils;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {
    }

    public static String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
