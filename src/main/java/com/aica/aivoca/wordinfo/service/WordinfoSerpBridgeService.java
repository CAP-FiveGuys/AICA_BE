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
import com.aica.aivoca.wordinfo.dto.WordInfoDto; // ❗️ [추가] Fallback DTO 임포트
import com.aica.aivoca.word.repository.ExampleSentenceRepository;
import com.aica.aivoca.word.repository.MeaningPartOfSpeechRepository;
import com.aica.aivoca.word.repository.MeaningRepository;
import com.aica.aivoca.word.repository.PartOfSpeechRepository;
import com.aica.aivoca.word.repository.WordRepository;
import com.aica.aivoca.wordinfo.external.NaverSerpParser;
import com.aica.aivoca.wordinfo.external.OpenAiLexiconClient;
import com.aica.aivoca.wordinfo.external.SerpApiClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors; // ❗️ [추가] Collectors.joining()

/**
 * [최종 수정본 v3 - 팀원 방식 적용]
 * - ❗️ POS_ID_MAP 삭제
 * - ❗️ '품사 통합 문자열' (예: "한정사, 대명사")을 PartOfSpeech 테이블에서 "findOrCreate" 방식으로 저장
 * - ❗️ DTO 변환 시 mpsRepository를 JOIN하여 'name'을 읽어오도록 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordinfoSerpBridgeService {

    private final WordRepository wordRepository;
    private final MeaningRepository meaningRepository;
    private final PartOfSpeechRepository partOfSpeechRepository; // ❗️ [사용]
    private final MeaningPartOfSpeechRepository mpsRepository; // ❗️ [사용]
    private final ExampleSentenceRepository exampleSentenceRepository;

    private final SerpApiClient serpApiClient;
    private final NaverSerpParser naverSerpParser;
    private final OpenAiLexiconClient openAiLexiconClient;

    private final WordinfoService wordinfoService; // Fallback용

    // --- ❗️ [삭제] ---
    // private static final Map<String, Long> POS_ID_MAP = ... (이 변수 전체를 삭제하세요!)
    // --- [삭제] 끝 ---


    /**
     * 컨트롤러가 호출하는 메인 메서드 (v2와 동일)
     */
    public SuccessStatusResponse<List<WordGetResponseDto>> lookupAndSaveWordIfNeededUsingSerpFirst(String wordText) {
        long t0 = System.currentTimeMillis();

        try {
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

            Word savedWord = attemptSerpSaveTransactionally(wordText, t0);

            return SuccessStatusResponse.of(
                    SuccessMessage.WORD_SAVED_FROM_NAVER_DICT,
                    List.of(toDto(savedWord))
            );

        } catch (Exception e) {
            log.warn("[SERP-BRIDGE] Serp-to-DB pipeline failed, falling back to GPT. word='{}' cause={}", wordText, e.toString());
            return fallbackToOldPipeline(wordText);
        }
    }

    /**
     * @Transactional이 붙은 핵심 저장 로직 (v2와 동일)
     */
    @Transactional
    public Word attemptSerpSaveTransactionally(String wordText, long t0) throws Exception {
        if (wordText == null || wordText.isBlank()) {
            throw new RuntimeException("Word text is null or blank, cannot attempt Serp save.");
        }

        final String html;
        String query = wordText + " 뜻";
        html = serpApiClient.fetchHtml(query);
        log.info("[SERP-BRIDGE] SerpAPI OK word='{}' htmlLen={}", wordText,
                html != null ? html.length() : -1);

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

        // ❗️ [수정] saveFromSerpWithFill 호출 (새로운 로직)
        Word saved = saveFromSerpWithFill(parsed, wordText);

        log.info("[SERP-BRIDGE] SAVE FROM SERP word='{}' elapsed={}ms",
                wordText, System.currentTimeMillis() - t0);
        return saved;
    }

    /**
     * ❗️ [수정] saveFromSerpWithFill (v3 - 팀원 방식)
     * - POS_ID_MAP 대신, 품사 문자열을 'findOrCreate' 방식으로 PartOfSpeech 테이블에 저장
     */
    private Word saveFromSerpWithFill(Map<String, Object> parsed, String wordText) {
        String lemma = opt((String) parsed.get("word"));
        if (lemma.isEmpty()) lemma = wordText;
        final String lemmaFinal = lemma;

        Word word = wordRepository.findByWordIgnoreCase(lemmaFinal)
                .orElseGet(() -> wordRepository.save(new Word(null, lemmaFinal)));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> meanings =
                (List<Map<String, Object>>) parsed.getOrDefault("meanings", List.of());

        for (Map<String, Object> m : meanings) {
            String meaningKo = opt((String) m.get("meaning"));
            if (meaningKo.isEmpty()) continue;
            final String meaningKoFinal = meaningKo;

            // --- ❗️ [수정] 품사 저장 로직 (팀원 방식 적용) ---

            // 1. 파싱된 품사 리스트 (예: ["한정사", "대명사"])
            @SuppressWarnings("unchecked")
            List<String> posList = (List<String>) m.getOrDefault("partsOfSpeech", List.of());

            // 2. 리스트를 하나의 '통합 문자열'로 합침 (예: "한정사, 대명사")
            String combinedPosString = posList.stream()
                    .map(this::opt) // 공백 제거
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", ")); // ", "로 연결

            final String posKoFinal = combinedPosString; // GPT 호출 시 사용

            // 3. Meaning 엔티티 저장 (품사 없이)
            Meaning meaningEntity = meaningRepository.save(
                    Meaning.builder()
                            .word(word)
                            .mean(meaningKoFinal)
                            // .partsOfSpeech()는 여기 없음 (정규화 방식)
                            .build()
            );

            // 4. '통합 품사 문자열'이 비어있지 않다면, DB에 저장 및 연결
            if (!posKoFinal.isEmpty()) {
                // 4-A. PartOfSpeech 테이블에서 '통합 문자열' 검색, 없으면 새로 저장(INSERT)
                PartOfSpeech pos = partOfSpeechRepository.findByName(posKoFinal)
                        .orElseGet(() -> {
                            log.info("[SERP-BRIDGE] New PartOfSpeech found, saving: '{}'", posKoFinal);
                            return partOfSpeechRepository.save(new PartOfSpeech(posKoFinal));
                        });

                // 4-B. Meaning_PartOfSpeech (중간 테이블)에 '뜻'과 '품사 ID'를 연결
                mpsRepository.save(
                        MeaningPartOfSpeech.builder()
                                .meaning(meaningEntity)
                                .partOfSpeech(pos)
                                .build()
                );
            }
            // --- [수정] 끝 ---


            // 예문 파트 (이하 로직은 기존과 동일)
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
                        .orElseThrow(() -> new RuntimeException(
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
                continue;
            }

            // (2) 예문은 있는데 해석이 없을 때 → GPT 번역
            if (ko.isEmpty()) {
                String translated = openAiLexiconClient.translateSentence(en);
                if (translated.isEmpty()) {
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
     * Fallback 로직 (v2와 동일, WordInfoDto 변환 포함)
     */
    private SuccessStatusResponse<List<WordGetResponseDto>> fallbackToOldPipeline(String wordText) {
        try {
            SuccessStatusResponse<List<WordInfoDto>> responseFromFallback =
                    wordinfoService.lookupAndSaveWordIfNeeded(wordText);

            List<WordGetResponseDto> convertedList = responseFromFallback.data().stream()
                    .map(wordInfoDto -> {
                        return new WordGetResponseDto(
                                wordInfoDto.wordId(),
                                null,
                                wordInfoDto.word(),
                                wordInfoDto.meanings()
                        );
                    }).toList();

            return new SuccessStatusResponse<>(
                    responseFromFallback.code(),
                    responseFromFallback.message(),
                    convertedList
            );

        } catch (CustomException e) {
            log.warn("[SERP-BRIDGE] Fallback validation failed: {}", e.getErrorMessage().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[SERP-BRIDGE] FATAL: Fallback (WordinfoService) also failed!", e);
            throw new RuntimeException("Fallback service failed", e);
        }
    }

    private String opt(String s) {
        return (s == null) ? "" : s.trim();
    }

    /**
     * ❗️ [수정] toDto (v3 - 팀원 방식)
     * - mpsRepository를 통해 '연결된' PartOfSpeech '이름'을 가져오도록 수정
     */
    private WordGetResponseDto toDto(Word word) {
        List<Meaning> meanings = meaningRepository.findAllByWord(word);

        List<MeaningDto> meaningDtos = meanings.stream().map(m -> {

            // --- ❗️ [수정] DTO의 List<String> parts 생성 로직 ---
            List<String> parts = mpsRepository.findAllByMeaning(m).stream()
                    .map(mp -> mp.getPartOfSpeech().getName()) // 예: "한정사, 대명사"
                    .toList();
            // --- [수정] 끝 ---

            List<ExampleSentenceDto> examples = exampleSentenceRepository.findAllByMeaning(m).stream()
                    .map(e -> new ExampleSentenceDto(e.getExamSentence(), e.getExamMeaning()))
                    .toList();

            return new MeaningDto(m.getMean(), parts, examples);
        }).toList();

        // ❗️ 00님의 DTO(sentenceId 포함된 4개 인수)에 맞게 반환
        return new WordGetResponseDto(word.getId(), null, word.getWord(), meaningDtos);
    }
}