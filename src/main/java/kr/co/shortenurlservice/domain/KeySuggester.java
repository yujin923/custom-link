package kr.co.shortenurlservice.domain;

import java.util.List;

public interface KeySuggester {
    List<String> suggest(String originalUrl);
}
