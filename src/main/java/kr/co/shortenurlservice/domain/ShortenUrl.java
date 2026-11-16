package kr.co.shortenurlservice.domain;

import java.util.Random;
import java.util.Set;

public class ShortenUrl {
    public static final String CUSTOM_KEY_REGEX = "^[a-zA-Z0-9-]{4,20}$";
    public static final int CUSTOM_KEY_MAX_LENGTH = 20;

    private static final Set<String> RESERVED_KEYS = Set.of(
            "shortenurl", "suggest", "api", "admin", "error", "login", "logout"
    );

    public static boolean isReservedKey(String shortenUrlKey) {
        return RESERVED_KEYS.contains(shortenUrlKey.toLowerCase());
    }

    public static boolean isValidCustomKeyFormat(String shortenUrlKey) {
        return shortenUrlKey != null && shortenUrlKey.matches(CUSTOM_KEY_REGEX);
    }

    private String originalUrl;
    private  String shortenUrlKey;
    private Long redirectCount;

    public ShortenUrl(String originalUrl, String shortenUrlKey) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = 0L;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectCount() {
        return redirectCount;
    }

    public void increaseRedirectCount() {
        this.redirectCount = this.redirectCount + 1;
    }

    public static String generateShortenUrlKey() {
        String base56characters = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        Random random = new Random();
        StringBuilder shortenUrlKey = new StringBuilder();

        for (int count = 0; count < 8; count++) {
            int base56charactersIndex = random.nextInt(0, base56characters.length());
            char base56Character = base56characters.charAt(base56charactersIndex);
            shortenUrlKey.append(base56Character);
        }

        return shortenUrlKey.toString();
    }
}
