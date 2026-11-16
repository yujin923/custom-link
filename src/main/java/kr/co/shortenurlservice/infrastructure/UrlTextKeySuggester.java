package kr.co.shortenurlservice.infrastructure;

import kr.co.shortenurlservice.domain.KeySuggester;
import kr.co.shortenurlservice.domain.ShortenUrl;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class UrlTextKeySuggester implements KeySuggester {

    private static final int MIN_KEY_LENGTH = 4;

    private static final Set<String> SECOND_LEVEL_SUFFIXES = Set.of(
            "co", "or", "ac", "go", "ne", "re", "com", "net", "org", "edu", "gov"
    );

    private static final Set<String> IGNORED_SUBDOMAINS = Set.of(
            "www", "m", "mobile", "web", "app"
    );

    private static final Set<String> IGNORED_PATH_WORDS = Set.of(
            "index", "home", "main", "view", "viewform", "watch", "post", "posts",
            "p", "d", "e", "s", "u", "en", "ko", "kr", "forms", "course", "courses",
            "article", "articles", "detail", "page", "pages", "list", "search", "edit", "share"
    );

    @Override
    public List<String> suggest(String originalUrl) {
        URI uri;
        try {
            uri = new URI(originalUrl.trim());
        } catch (URISyntaxException | NullPointerException e) {
            return List.of();
        }

        String host = uri.getHost();
        if (host == null) {
            return List.of();
        }

        List<String> hostLabels = new ArrayList<>(Arrays.asList(host.toLowerCase().split("\\.")));
        if (hostLabels.size() > 1) {
            hostLabels.remove(hostLabels.size() - 1);
        }
        if (hostLabels.size() > 1 && SECOND_LEVEL_SUFFIXES.contains(hostLabels.get(hostLabels.size() - 1))) {
            hostLabels.remove(hostLabels.size() - 1);
        }
        String siteName = normalize(hostLabels.get(hostLabels.size() - 1));
        String subdomain = null;
        if (hostLabels.size() > 1) {
            String candidate = normalize(hostLabels.get(hostLabels.size() - 2));
            if (!IGNORED_SUBDOMAINS.contains(candidate)) {
                subdomain = candidate;
            }
        }

        String lastPathWord = null;
        String path = uri.getPath() == null ? "" : uri.getPath();
        for (String segment : path.split("/")) {
            String word = normalize(segment.replaceAll("\\.[A-Za-z0-9]+$", ""));
            if (isMeaningfulPathWord(word)) {
                lastPathWord = word;
            }
        }

        Set<String> candidates = new LinkedHashSet<>();
        if (lastPathWord != null) {
            candidates.add(lastPathWord);
            candidates.add(siteName + "-" + lastPathWord);
        }
        if (subdomain != null) {
            candidates.add(siteName + "-" + subdomain);
        }
        candidates.add(siteName);

        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            String fitted = fitLength(candidate);
            if (!fitted.isEmpty() && !result.contains(fitted)) {
                result.add(fitted);
            }
        }
        return result;
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private boolean isMeaningfulPathWord(String word) {
        if (word.isEmpty() || word.length() > 40) return false;
        if (IGNORED_PATH_WORDS.contains(word)) return false;
        if (word.matches("\\d{5,}")) return false;
        if (looksRandom(word)) return false;
        return true;
    }

    private boolean looksRandom(String word) {
        return word.length() >= 6
                && !word.contains("-")
                && word.matches(".*\\d.*")
                && word.matches(".*[a-z].*");
    }

    private String fitLength(String key) {
        int max = ShortenUrl.CUSTOM_KEY_MAX_LENGTH;
        if (key.length() > max) {
            int cut = key.lastIndexOf('-', max);
            key = (cut >= MIN_KEY_LENGTH) ? key.substring(0, cut) : key.substring(0, max);
            key = key.replaceAll("-+$", "");
        }
        if (!key.isEmpty() && key.length() < MIN_KEY_LENGTH) {
            key = key + "-link";
        }
        return key;
    }
}
