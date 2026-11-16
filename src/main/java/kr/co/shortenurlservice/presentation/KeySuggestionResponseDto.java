package kr.co.shortenurlservice.presentation;

import java.util.List;

public class KeySuggestionResponseDto {
    private List<String> suggestions;

    public KeySuggestionResponseDto(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
