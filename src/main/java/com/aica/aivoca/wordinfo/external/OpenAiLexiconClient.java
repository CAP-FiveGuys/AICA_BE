package com.aica.aivoca.wordinfo.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OpenAiLexiconClient {

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    // =========================================================
    // 공통 Chat 호출
    // =========================================================
    private Map<String, Object> chat(String system, String user, double temperature) throws Exception {
        String body = om.writeValueAsString(Map.of(
                "model", "gpt-3.5-turbo-0125",   // 너가 쓰고 있던 모델명 그대로
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                ),
                "temperature", temperature
        ));

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("OpenAI error status=" + res.statusCode() + " body=" + res.body());
        }

        Map<String, Object> raw = om.readValue(res.body(), Map.class);
        Map<String, Object> choice0 = ((List<Map<String, Object>>) raw.get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choice0.get("message");
        String content = (String) message.get("content");

        Map<String, Object> out = new HashMap<>();
        out.put("content", content);
        return out;
    }

    // =========================================================
    // 1) 예문만 여러 개 생성 (기존에 있던 거)
    // =========================================================
    // meaningsTop1: [{ "meaning":"...", "partOfSpeech":"명사" }, ...]
    public List<Map<String, Object>> generateExamplesOnly(String queryWord, List<Map<String, Object>> meaningsTop1) {
        try {
            String system = """
너는 "예문 생성 보조기"다.

[역할]
- 주어진 표제어(HEADWORD)와 각 뜻/품사에 대해, 뜻당 영어 예문 1개와 한국어 해석 1개를 만든다.

[강제 규칙]
- 예문에 표제어를 '그대로' 포함해야 한다. (변형 금지: 과거형, 3인칭 단수, 복수형, -ing 등 사용 금지)
- 관사는 허용(예: a/an/the), 대소문자는 자유. 단 표제어는 한 단어로 분리되어 보여야 한다.
- 길이: 6~18단어, CEFR A2~B1 수준, 자연스러운 일상 문장.
- 고유명사/지명/브랜드명/따옴표/콜론/세미콜론 사용 금지.
- "example", "meaning", "this sense/word/meaning" 같은 메타 문구 사용 금지.
- 입력으로 주어진 meaning/partOfSpeech는 수정/요약/삭제 금지.

[출력 형식 (JSON 배열만 출력)]
입력 순서를 그대로 유지하여 다음 스키마로 출력:
[
  {
    "meaning": "<입력 그대로의 한국어 뜻>",
    "partOfSpeech": "<입력 그대로의 품사(한글)>",
    "example": { "sentence": "<영어 1문장(표제어 원형 그대로 포함)>", "meaning": "<한국어 해석>" }
  }
]
""";

            String user = """
[HEADWORD]
%s

[MEANINGS_TOP1_JSON]
%s
""".formatted(queryWord, om.writeValueAsString(meaningsTop1));

            Map<String, Object> res = chat(system, user, 0.2);
            String content = (String) res.get("content");
            return om.readValue(content, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // =========================================================
    // 2) 예문 1개 만들기 (기존에 있던 거)
    // =========================================================
    public Optional<Map<String, String>> generateOneExample(String queryWord, String meaningKo, String posKo) {
        try {
            String system = """
너는 "예문 생성 보조기"다.

[역할]
- 주어진 표제어(HEADWORD), 한국어 뜻, 품사(한글)에 대해 영어 예문 1개와 한국어 해석 1개를 만든다.

[강제 규칙]
- 예문에 표제어를 '그대로' 포함해야 한다. (변형 금지: 과거형, 3인칭 단수, 복수형, -ing 등 사용 금지)
- 관사는 허용, 대소문자 자유. 표제어는 한 단어로 분리되어 보여야 한다.
- 길이: 6~18단어, CEFR A2~B1 수준, 자연스러운 일상 문장.
- 고유명사/지명/브랜드명/따옴표/콜론/세미콜론 사용 금지.
- "example", "meaning", "this sense/word/meaning" 같은 메타 문구 사용 금지.
- 입력으로 주어진 뜻/품사는 수정 금지.

[출력 형식 (JSON만 출력)]
{ "sentence":"<영어 1문장(표제어 원형 그대로 포함)>", "meaning":"<한국어 해석>" }
""";

            String user = """
[HEADWORD]
%s

[POS]
%s

[KOR_MEANING]
%s
""".formatted(queryWord, posKo, meaningKo);

            Map<String, Object> res = chat(system, user, 0.3);
            String content = (String) res.get("content");
            Map<String, String> obj = om.readValue(content, Map.class);
            return Optional.of(obj);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // =========================================================
    // 3) 📌 새로 추가하는 거: 영어 예문 → 한국어 번역
    // =========================================================
    public String translateSentence(String sentence) {
        try {
            String system = """
너는 영어 문장을 자연스럽고 간단한 한국어로 번역하는 번역기다.
- 존댓말 말고 평서형 구어체로 번역한다.
- 불필요한 설명을 덧붙이지 않는다.
- 오직 번역문만 출력한다.
""";
            String user = sentence;
            Map<String, Object> res = chat(system, user, 0.1);
            return (String) res.get("content");
        } catch (Exception e) {
            // 번역 실패하면 그냥 빈 문자열 돌려줌
            return "";
        }
    }
}
