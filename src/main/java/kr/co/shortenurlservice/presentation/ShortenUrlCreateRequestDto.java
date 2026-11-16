package kr.co.shortenurlservice.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kr.co.shortenurlservice.domain.ShortenUrl;
import org.hibernate.validator.constraints.URL;

public class ShortenUrlCreateRequestDto {
    @NotNull
    @URL(regexp = "[(http(s)?):\\/\\/(www.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)")
    private String originalUrl;

    @Pattern(regexp = ShortenUrl.CUSTOM_KEY_REGEX,
            message = "customKey는 영문, 숫자, 하이픈(-)으로 4~20자여야 합니다.")
    private String customKey;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getCustomKey() {
        return customKey;
    }
}
