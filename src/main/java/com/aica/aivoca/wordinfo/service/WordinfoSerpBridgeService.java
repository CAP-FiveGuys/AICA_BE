package com.aica.aivoca.wordinfo.service;

import com.aica.aivoca.domain.ExampleSentence;
import com.aica.aivoca.domain.Meaning;
import com.aica.aivoca.domain.MeaningPartOfSpeech;
import com.aica.aivoca.domain.PartOfSpeech;
import com.aica.aivoca.domain.Word;
import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.word.dto.ExampleSentenceDto;
import com.aica.aivoca.word.dto.MeaningDto;
import com.aica.aivoca.word.dto.WordGetResponseDto;
import com.aica.aivoca.word.repository.ExampleSentenceRepository;
import com.aica.aivoca.word.repository.MeaningPartOfSpeechRepository;
import com.aica.aivoca.word.repository.MeaningRepository;
import com.aica.aivoca.word.repository.PartOfSpeechRepository;
import com.aica.aivoca.word.repository.WordRepository;
import com.aica.aivoca.wordinfo.dto.WordInfoDto;
import com.aica.aivoca.wordinfo.external.NaverSerpParser;
import com.aica.aivoca.wordinfo.external.OpenAiLexiconClient;
import com.aica.aivoca.wordinfo.external.SerpApiClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * [최종 수정본 v2]
 * - ❗️ 버그 1: lookup 메서드의 불필요한 null/blank 체크 제거 (유효성 검사는 Fallback 서비스가 하도록 위임)
 * - ❗️ 버그 2: fallback 로직이 CustomException을 숨기지 않고 다시 던지도록 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordinfoSerpBridgeService {
    // (... Repository, Client, Service 주입은 동일 ...)
    private final WordRepository wordRepository;
    private final MeaningRepository meaningRepository;
    private final PartOfSpeechRepository partOfSpeechRepository;
    private final MeaningPartOfSpeechRepository mpsRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;

    private final SerpApiClient serpApiClient;
    private final NaverSerpParser naverSerpParser;
    private final OpenAiLexiconClient openAiLexiconClient;

    private final WordinfoService wordinfoService; // Fallback용

    private static final Map<String, Long> POS_ID_MAP = Map.ofEntries(
            Map.entry("명사", 1L), Map.entry("동사", 2L), Map.entry("형용사", 3L),
            Map.entry("부사", 4L), Map.entry("대명사", 5L), Map.entry("전치사", 6L),
            Map.entry("접속사", 7L), Map.entry("감탄사", 8L)
    );

    /**
     * 컨트롤러가 호출하는 메인 메서드
     * ❗️ [수정] 불필요한 null/blank 체크 제거
     */
    public SuccessStatusResponse<List<WordGetResponseDto>> lookupAndSaveWordIfNeededUsingSerpFirst(String wordText) {
        long t0 = System.currentTimeMillis();

        // --- ❗️ [수정] 200 OK를 반환하던 null/blank 체크 로직 삭제 ---
        // if (wordText == null || wordText.isBlank()) {
        //     return SuccessStatusResponse.of(SuccessMessage.GET_WORD_SUCCESS, List.of());
        // }
        // --- (유효성 검사는 fallbackToOldPipeline의 WordinfoService가 담당) ---

        // 0) DB 먼저
        // ❗️ wordText가 null일 수 있으므로, Null/Blank 체크가 필요한 서비스(wordinfoService)를 먼저 호출하도록 순서 변경
        //
        // Optional<Word> found = wordRepository.findByWordIgnoreCase(wordText); // -> wordText가 null이면 NPE 발생
        // if (found.isPresent()) {
        //     ...
        // }
        // (위 로직을 1-try 블록 안으로 이동)

        try {
            // ❗️ [수정] DB 조회 로직도 try 블록 안으로 이동
            if (wordText != null && !wordText.isBlank()) {
                Optional<Word> found = wordRepository.findByWordIgnoreCase(wordText);
                if (found.isPresent()) {
                    log.info("[SERP-BRIDGE] DB HIT word='{}'", wordText);
                    return SuccessStatusResponse.of(
                            SuccessMessage.WORD_FOUND_IN_DB,
                            List.of(toDto(found.get()))
                    );
                }
            }

            // 1~4 단계(파싱, GPT, 저장)를 트랜잭션으로 실행
            Word savedWord = attemptSerpSaveTransactionally(wordText, t0);

            // 2단계 성공 시
            return SuccessStatusResponse.of(
                    SuccessMessage.WORD_SAVED_FROM_NAVER_DICT,
                    List.of(toDto(savedWord))
            );

        } catch (Exception e) {
            // 2단계(Serp/Jsoup/GPT/DB) 중 '어디서든' 실패 시
            log.warn("[SERP-BRIDGE] Serp-to-DB pipeline failed, falling back to GPT. word='{}' cause={}", wordText, e.toString());
            // 3. Fallback 실행
            return fallbackToOldPipeline(wordText);
        }
    }

    /**
     * ❗️ [신규] @Transactional이 여기 붙습니다.
     * (기존과 동일)
     */
    @Transactional
    public Word attemptSerpSaveTransactionally(String wordText, long t0) throws Exception {
        // ❗️ [추가] wordText가 null이면 Jsoup 파싱이 실패하므로 여기서 방어
        if (wordText == null || wordText.isBlank()) {
            throw new RuntimeException("Word text is null or blank, cannot attempt Serp save.");
        }

        // 1) SerpAPI (네이버 검색)
        final String html;
        String query = wordText + " 뜻";
        html = serpApiClient.fetchHtml(query);
        log.info("[SERP-BRIDGE] SerpAPI OK word='{}' htmlLen={}", wordText,
                html != null ? html.length() : -1);

        // ... (이하 동일) ...
        final Map<String, Object> parsed;
        parsed = naverSerpParser.parse(html, query);
        log.info("[SERP-BRIDGE] parsed={}", parsed);

        Object ok = parsed.get("ok");
        Object match = parsed.get("matches_query");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> meanings =
                (List<Map<String, Object>>) parsed.getOrDefault("meanings", List.of());

        if (!Boolean.TRUE.equals(ok) || !Boolean.TRUE.equals(match) || meanings.isEmpty()) {
            throw new RuntimeException("Parsed but empty/nomatch");
        }

        Word saved = saveFromSerpWithFill(parsed, wordText);
        log.info("[SERP-BRIDGE] SAVE FROM SERP word='{}' elapsed={}ms",
                wordText, System.currentTimeMillis() - t0);
        return saved;
    }

    /**
     * ❗️ [수정] '조용한 실패'를 '시끄러운 실패(Exception)'로 변경
     * (기존과 동일)
     */
    private Word saveFromSerpWithFill(Map<String, Object> parsed, String wordText) {
        // ... (이 메서드 내용은 변경 없음) ...

        // 단어(lemma)
        String lemma = opt((String) parsed.get("word"));
        if (lemma.isEmpty()) lemma = wordText;
        final String lemmaFinal = lemma; // 람다에서 사용

        // word 테이블에 없으면 생성
        Word word = wordRepository.findByWordIgnoreCase(lemmaFinal)
                .orElseGet(() -> wordRepository.save(new Word(null, lemmaFinal)));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> meanings =
                (List<Map<String, Object>>) parsed.getOrDefault("meanings", List.of());

        for (Map<String, Object> m : meanings) {
            String meaningKo = opt((String) m.get("meaning"));
            if (meaningKo.isEmpty()) continue;
            final String meaningKoFinal = meaningKo;

            Meaning meaningEntity = meaningRepository.save(new Meaning(null, word, meaningKoFinal));

            // 품사 1개만 저장
            @SuppressWarnings("unchecked")
            List<String> posList = (List<String>) m.getOrDefault("partsOfSpeech", List.of());
            String posKo = posList.isEmpty() ? "" : opt(posList.get(0));
            final String posKoFinal = posKo;
            if (!posKoFinal.isEmpty()) {
                Long posId = POS_ID_MAP.get(posKoFinal);
                if (posId != null) {
                    PartOfSpeech pos = partOfSpeechRepository.findById(posId).orElse(null);
                    if (pos != null) {
                        mpsRepository.save(new MeaningPartOfSpeech(null, meaningEntity, pos));
                    }
                }
            }

            // 예문 파트
            @SuppressWarnings("unchecked")
            List<Map<String, String>> exs =
                    (List<Map<String, String>>) m.getOrDefault("exampleSentences", List.of());

            String en = "";
            String ko = "";

            if (!exs.isEmpty()) {
                Map<String, String> ex0 = exs.get(0);
                en = opt(ex0.get("sentence"));
                ko = opt(ex0.get("meaning"));
            }

            // (1) 예문 자체가 없을 때 → GPT로 예문+해석 생성
            if (en.isEmpty()) {
                Map<String, String> ex = openAiLexiconClient.generateOneExample(lemmaFinal, meaningKoFinal, posKoFinal)
                        .orElseThrow(() -> new RuntimeException( // ❗️ GPT 실패 시 트랜잭션 롤백 유도
                                "GPT example generation failed for: " + lemmaFinal
                        ));

                String s = opt(ex.get("sentence"));
                String k = opt(ex.get("meaning"));
                if (s.isEmpty() || k.isEmpty()) {
                    throw new RuntimeException("GPT generated invalid example (empty)");
                }

                exampleSentenceRepository.save(
                        new ExampleSentence(null, meaningEntity, s, k)
                );
                continue; // 다음 '뜻'으로 넘어감
            }

            // (2) 예문은 있는데 해석이 없을 때 → GPT 번역
            if (ko.isEmpty()) {
                String translated = openAiLexiconClient.translateSentence(en);
                if (translated.isEmpty()) { // ❗️ 번역 실패 시 롤백 유도
                    throw new RuntimeException("GPT translation failed for: " + en);
                }
                ko = translated;
            }

            // (3) 저장
            exampleSentenceRepository.save(
                    new ExampleSentence(null, meaningEntity, en, ko)
            );
        }
        return word;
    }

    /**
     * ❗️ [수정] Fallback 로직 (WordInfoDto -> WordGetResponseDto 변환 추가)
     * WordinfoService가 반환하는 <List<WordInfoDto>>를
     * 컨트롤러가 반환해야 하는 <List<WordGetResponseDto>>로 수동 변환합니다.
     */
    private SuccessStatusResponse<List<WordGetResponseDto>> fallbackToOldPipeline(String wordText) {
        try {
            // 1. Fallback 서비스 호출 (반환 타입: ...<List<WordInfoDto>>)
            SuccessStatusResponse<List<WordInfoDto>> responseFromFallback =
                    wordinfoService.lookupAndSaveWordIfNeeded(wordText);

            // 2. ❗️ 수동 변환 ❗️: List<WordInfoDto> -> List<WordGetResponseDto>
            List<WordGetResponseDto> convertedList = responseFromFallback.data().stream()
                    .map(wordInfoDto -> {
                        // WordInfoDto(3개 인수)를 WordGetResponseDto(4개 인수)로 변환
                        return new WordGetResponseDto(
                                wordInfoDto.wordId(),
                                null,                   // sentenceId는 null로 채움
                                wordInfoDto.word(),
                                wordInfoDto.meanings()
                        );
                    }).toList();

            // 3. 변환된 DTO 리스트로 새 응답 객체를 만들어 반환
            return new SuccessStatusResponse<>(
                    responseFromFallback.code(),
                    responseFromFallback.message(),
                    convertedList
            );

        } catch (CustomException e) {
            // 4) ❗️ 1번이 'CustomException'을 던지면(유효성 검사 실패),
            //    그 예외를 '숨기지 말고' 컨트롤러로 다시 던집니다!
            log.warn("[SERP-BRIDGE] Fallback validation failed: {}", e.getErrorMessage().getMessage());
            throw e;
        } catch (Exception e) {
            // 5) 그 외 예상치 못한 오류 (NPE, AI 통신 오류 등)
            log.error("[SERP-BRIDGE] FATAL: Fallback (WordinfoService) also failed!", e);
            throw new RuntimeException("Fallback service failed", e);
        }
    }

    private String opt(String s) {
        return (s == null) ? "" : s.trim();
    }

    // DTO 변환 (DB HIT 때 사용)
    private WordGetResponseDto toDto(Word word) {
        List<Meaning> meanings = meaningRepository.findAllByWord(word);

        List<MeaningDto> meaningDtos = meanings.stream().map(m -> {
            List<String> parts = mpsRepository.findAllByMeaning(m).stream()
                    .map(mp -> mp.getPartOfSpeech().getName())
                    .toList();

            List<ExampleSentenceDto> examples = exampleSentenceRepository.findAllByMeaning(m).stream()
                    .map(e -> new ExampleSentenceDto(e.getExamSentence(), e.getExamMeaning()))
                    .toList();

            return new MeaningDto(m.getMean(), parts, examples);
        }).toList();

        // ❗️ [수정] 4개 인수에 맞게 변경. sentenceId는 null로 전달
        return new WordGetResponseDto(word.getId(), null, word.getWord(), meaningDtos);
    }
}