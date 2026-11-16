package kr.co.shortenurlservice.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlTextKeySuggesterTest {

    private final UrlTextKeySuggester suggester = new UrlTextKeySuggester();

    @Test
    void 경로의_마지막_단어와_사이트_이름을_추천한다() {
        assertEquals(
                List.of("spring-boot", "github-spring-boot", "github"),
                suggester.suggest("https://github.com/spring-projects/spring-boot"));
    }

    @Test
    void www와_의미없는_경로는_무시한다() {
        assertEquals(
                List.of("youtube"),
                suggester.suggest("https://www.youtube.com/watch?v=abc123"));
    }

    @Test
    void 랜덤ID처럼_보이는_경로는_무시하고_서브도메인을_쓴다() {
        assertEquals(
                List.of("google-docs", "google"),
                suggester.suggest("https://docs.google.com/forms/d/e/1FAIpQLSfAbC123/viewform"));
    }

    @Test
    void co_kr_같은_2단계_도메인을_처리한다() {
        assertEquals(
                List.of("coupang"),
                suggester.suggest("https://www.coupang.co.kr"));
    }

    @Test
    void 글번호_같은_숫자_경로는_무시한다() {
        assertEquals(
                List.of("someuser", "naver-someuser", "naver-blog", "naver"),
                suggester.suggest("https://blog.naver.com/someuser/223456789"));
    }

    @Test
    void 너무_짧은_키는_link를_붙인다() {
        assertEquals(List.of("x-link"), suggester.suggest("https://x.com"));
    }

    @Test
    void 너무_긴_키는_하이픈_경계에서_20자_이내로_자른다() {
        List<String> result = suggester.suggest(
                "https://example.com/blog/how-to-build-a-url-shortener-with-spring");
        assertEquals("how-to-build-a-url", result.get(0));
        result.forEach(key -> assertTrue(key.length() <= 20, key));
    }

    @Test
    void 잘못된_URL이면_빈_목록을_돌려준다() {
        assertEquals(List.of(), suggester.suggest("이건 URL이 아님"));
        assertEquals(List.of(), suggester.suggest(null));
    }
}
