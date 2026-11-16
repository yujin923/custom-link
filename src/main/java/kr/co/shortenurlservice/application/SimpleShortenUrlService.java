package kr.co.shortenurlservice.application;

import kr.co.shortenurlservice.domain.DuplicateShortenUrlKeyException;
import kr.co.shortenurlservice.domain.KeySuggester;
import kr.co.shortenurlservice.domain.LackOfShortenUrlKeyException;
import kr.co.shortenurlservice.domain.ReservedShortenUrlKeyException;
import kr.co.shortenurlservice.domain.NotFoundShortenUrlException;
import kr.co.shortenurlservice.domain.ShortenUrl;
import kr.co.shortenurlservice.domain.ShortenUrlRepository;
import kr.co.shortenurlservice.presentation.KeySuggestionResponseDto;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateRequestDto;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateResponseDto;
import kr.co.shortenurlservice.presentation.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;
    private KeySuggester keySuggester;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository, KeySuggester keySuggester) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.keySuggester = keySuggester;
    }

    public KeySuggestionResponseDto suggestShortenUrlKeys(String originalUrl) {
        final int MAX_SUGGESTION_COUNT = 3;
        List<String> suggestions = new ArrayList<>();

        for (String candidate : keySuggester.suggest(originalUrl)) {
            if (suggestions.size() >= MAX_SUGGESTION_COUNT) {
                break;
            }
            String availableKey = findAvailableKey(candidate);
            if (null != availableKey && !suggestions.contains(availableKey)) {
                suggestions.add(availableKey);
            }
        }

        return new KeySuggestionResponseDto(suggestions);
    }

    private String findAvailableKey(String candidate) {
        final int MAX_SUFFIX_NUMBER = 9;

        for (int number = 1; number <= MAX_SUFFIX_NUMBER; number++) {
            String key = (number == 1) ? candidate : withSuffix(candidate, "-" + number);

            if (!ShortenUrl.isValidCustomKeyFormat(key)) {
                return null;
            }
            if (ShortenUrl.isReservedKey(key)) {
                continue;
            }
            if (null == shortenUrlRepository.findShortenUrlByShortenUrlKey(key)) {
                return key;
            }
        }
        return null;
    }

    private String withSuffix(String key, String suffix) {
        int maxBaseLength = ShortenUrl.CUSTOM_KEY_MAX_LENGTH - suffix.length();
        if (key.length() > maxBaseLength) {
            key = key.substring(0, maxBaseLength).replaceAll("-+$", "");
        }
        return key + suffix;
    }
    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ) {
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        String customKey = shortenUrlCreateRequestDto.getCustomKey();

        ShortenUrl shortenUrl;
        if (null != customKey) {
            shortenUrl = saveWithCustomKey(originalUrl, customKey);
        } else {
            shortenUrl = saveWithRandomKey(originalUrl);
        }

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    private ShortenUrl saveWithCustomKey(String originalUrl, String customKey) {
        if (ShortenUrl.isReservedKey(customKey)) {
            throw new ReservedShortenUrlKeyException();
        }

        ShortenUrl shortenUrl = new ShortenUrl(originalUrl, customKey);
        boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(shortenUrl);

        if (!saved) {
            throw new DuplicateShortenUrlKeyException();
        }

        return shortenUrl;
    }

    private ShortenUrl saveWithRandomKey(String originalUrl) {
        final int MAX_RETRY_COUNT = 5;

        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);

            if (shortenUrlRepository.saveShortenUrlIfAbsent(shortenUrl)) {
                return shortenUrl;
            }
        }

        throw new LackOfShortenUrlKeyException();
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null == shortenUrl) {
            throw new NotFoundShortenUrlException();
        }

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null == shortenUrl) {
            throw new NotFoundShortenUrlException();
        }

        shortenUrl.increaseRedirectCount();
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        return shortenUrl.getOriginalUrl();
    }
}
