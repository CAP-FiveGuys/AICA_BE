package com.aica.aivoca.word.external;

import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.word.external.config.OpenAiProperties;
import com.aica.aivoca.word.external.dto.*;
import com.aica.aivoca.word.external.dto.OpenAiRequest.ChatMessage;
import com.aica.aivoca.word.external.dto.OpenAiRequest.ChatMessage.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AiDictionaryClientImpl implements AiDictionaryClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    @Override
    public AiWordResponse getWordInfo(String word) {
        String prompt = """
                단어: %s
                
                아래 JSON 형식에 맞춰 응답해줘. 대표적인 뜻 몇 개만 제공해도 괜찮아.
                반드시 모든 'meaning'은 **한국어로 번역된 뜻**이어야 해.
                각 뜻에는 해당되는 모든 품사(partsOfSpeech)를 배열로 포함시켜줘.
                그리고 각 뜻에 대해 그 뜻이 포함된 예문(exampleSentences)을 최소 하나씩 제공해줘.
                
                JSON 형식 예시:
                {
                  "meanings": [
                    {
                      "meaning": "뜻",
                      "partsOfSpeech": ["명사", "동사", 등],
                      "exampleSentences": [
                        {
                          "sentence": "영어 예문",
                          "sentenceMeaning": "예문을 한국어로 해석"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(word);

        OpenAiRequest request = new OpenAiRequest(
                "gpt-3.5-turbo",
                new ChatMessage[]{new ChatMessage(Role.USER, prompt)}
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(request, headers);

        try {//OpenAI에 POST 요청을 보냄
            OpenAiResponse response = restTemplate.postForObject(
                    openAiProperties.getUrl(), entity, OpenAiResponse.class);

            //응답이 null이거나 choices(응답에서 ai가 생성한 결과물들이 담긴 리스트)가 비어있는 경우
            if (response == null || response.choices().isEmpty()) {
                throw new CustomException(ErrorMessage.OPENAI_RESPONSE_EMPTY);
            }
            //AI가 생성한 JSON텍스트를 꺼내는 부분
            String content = response.getFirstMessageContent();
            return objectMapper.readValue(content, AiWordResponse.class);

        } catch (HttpClientErrorException e) {//HTTP 요청 자체에서 에러 발생 (ex: 401 Unauthorized, 400 Bad Request 등) 시 처리
            throw new CustomException(ErrorMessage.OPENAI_REQUEST_FAILED);
        } catch (JsonProcessingException e) {//JSON 문자열 → 객체 변환 실패 시 처리 (예: 형식 불일치, 누락 등)
            throw new CustomException(ErrorMessage.OPENAI_RESPONSE_PARSE_FAILED);
        } catch (Exception e) {//위 두 케이스 외의 예상하지 못한 모든 오류를 처리 (예: DB 문제, NullPointer 등)
            throw new CustomException(ErrorMessage.WORD_LOOKUP_FAILED);
        }
    }
}
