package com.aica.aivoca.voca.service;

import com.aica.aivoca.voca.exception.TooManyRequestsException;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiExampleGenerationService implements ExampleGenerationService {

    private final OpenAiService openAiService;

    public AiExampleGenerationService(@Value("${openai.api-key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    @Override
    public String generateExample(String word) {
        System.out.println("ChatGPT 예문 생성 요청: " + word);

        String prompt = String.format("Please generate a natural English sentence using the word '%s'. Only return the sentence.", word);

        CompletionRequest request = CompletionRequest.builder()
                .prompt(prompt)
                .model("gpt-3.5-turbo-instruct")  // gpt-3.5-turbo 계열에서 Completion API 사용 가능 모델
                .maxTokens(60)
                .temperature(0.7)
                .build();

        int retries = 3;
        long delay = 1000;

        for (int i = 0; i < retries; i++) {
            try {
                String example = openAiService.createCompletion(request)
                        .getChoices()
                        .get(0)
                        .getText()
                        .trim();
                return example;
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("HTTP 429")) {
                    System.out.println("Too many requests. Retrying in " + delay + "ms...");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    delay *= 2;
                } else {
                    e.printStackTrace();
                    throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
                }
            }
        }

        throw new TooManyRequestsException("OpenAI API 트래픽이 너무 많아 예문 생성을 실패했습니다.");
    }
}
