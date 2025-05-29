package com.aica.aivoca.wordinfo.external;

import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.wordinfo.config.OpenAiProperties;
import com.aica.aivoca.wordinfo.dto.AiWordResponse;
import com.aica.aivoca.wordinfo.dto.OpenAiRequest;
import com.aica.aivoca.wordinfo.dto.OpenAiResponse;
import com.aica.aivoca.wordinfo.dto.OpenAiRequest.ChatMessage;
import com.aica.aivoca.wordinfo.dto.OpenAiRequest.ChatMessage.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
                
                아래 조건에 따라 응답해줘:
                
                ⚠️ 반드시 **네이버 영어사전**의 정보를 참고해서만 응답하고, 스스로 내용을 **생성하지 마**.
                
                1. 입력된 단어가 **실제로 존재하는 단어**라면:
                - 대표적인 뜻 몇 개만 제공해도 괜찮아.
                - 반드시 모든 'meaning'은 **한국어로 번역된 뜻**이어야 해.
                - 각 뜻에는 해당되는 모든 품사(partsOfSpeech)를 배열로 포함시켜줘.
                - 그리고 각 뜻에 대해 그 뜻이 포함된 예문(exampleSentences)을 각각 하나씩 제공해줘.
                
                이 경우 응답은 아래 JSON 형식을 따라줘:
                {
                  "meanings": [
                    {
                      "meaning": "뜻",
                      "partsOfSpeech": ["명사", "동사", ...],
                      "exampleSentences": [
                        {
                          "sentence": "영어 예문",
                          "sentenceMeaning": "예문을 한국어로 해석"
                        }
                      ]
                    }
                  ]
                }
                
                2. 입력된 단어가 **존재하지 않는 이상한 단어**라면:
                - 아래 형식처럼 단순히 에러 메시지만 응답해줘:
                
                {
                  "error": "이 단어는 사전에 등록되지 않은 단어입니다."
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
            System.out.println("GPT 응답 원문: " + content);
            // 사전 등록되지 않은 단어일 경우 커스텀 예외 발생
            JsonNode root = objectMapper.readTree(content);
            if (root.has("error")) {
                // 여기로 떨어져야 정상!
                throw new CustomException(ErrorMessage.WORD_NOT_FOUND_IN_DICTIONARY);
            }
            return objectMapper.treeToValue(root, AiWordResponse.class);

        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorMessage.OPENAI_REQUEST_FAILED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorMessage.OPENAI_RESPONSE_PARSE_FAILED);
        } catch (CustomException e) {
            throw e; // 이미 발생시킨 커스텀 예외는 그대로 다시 던짐
        } catch (Exception e) {
            throw new CustomException(ErrorMessage.WORD_LOOKUP_FAILED);
        }
    }
}
