package com.ossanasur.cbconnect.module.finance.util;

public final class FilenameSanitizer {

    private static final int MAX_LENGTH = 60;
    private static final String FALLBACK = "INCONNU";

    private FilenameSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) return FALLBACK;
        String s = input.trim()
                        .replace(' ', '_')
                        .replaceAll("[/\\\\:*?\"<>|]", "-");
        if (s.length() > MAX_LENGTH) s = s.substring(0, MAX_LENGTH);
        return s.isBlank() ? FALLBACK : s;
    }
}
