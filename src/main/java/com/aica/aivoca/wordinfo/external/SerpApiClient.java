package com.aica.aivoca.wordinfo.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * SerpAPI에서 네이버 검색결과를 가져오는 클라이언트
 * - HTML로 받는 메서드 (예전 방식)
 * - JSON으로 받는 메서드 (신규)
 * - JSON이 알려준 raw_html_file을 다시 다운받는 메서드
 */
@Component
@RequiredArgsConstructor
public class SerpApiClient {

    @Value("${serpapi.key}")
    private String apiKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 예전 방식: 그냥 HTML로 바로 받기
     * (지금 서비스에서 이걸 이미 부르고 있어서 없으면 컴파일 에러남)
     */
    public String fetchHtml(String query) {
        String url = "https://serpapi.com/search.html?engine=naver"
                + "&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&api_key=" + apiKey;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) {
                return res.body();
            }
            throw new RuntimeException("SerpApi error status=" + res.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("SerpApi fetch failed", e);
        }
    }

    /**
     * 새 방식: SerpAPI가 구조화해준 JSON으로 받기
     * (여기서 raw_html_file 주소도 얻을 수 있음)
     */
    public JsonNode fetchJson(String query) {
        String url = "https://serpapi.com/search?engine=naver"
                + "&output=json"
                + "&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&api_key=" + apiKey;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new RuntimeException("SerpApi JSON error status=" + res.statusCode() + " body=" + res.body());
            }
            return om.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("SerpApi JSON fetch failed", e);
        }
    }

    /**
     * JSON 안에 있는 raw_html_file 주소로 실제 HTML 다시 받기
     */
    public String fetchRawHtmlFromUrl(String rawHtmlUrl) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(rawHtmlUrl)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) {
                return res.body();
            }
            throw new RuntimeException("fetchRawHtmlFromUrl error status=" + res.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("fetchRawHtmlFromUrl failed", e);
        }
    }
}
