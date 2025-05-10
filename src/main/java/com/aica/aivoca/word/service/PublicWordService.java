package com.aica.aivoca.word.service;

import com.aica.aivoca.domain.*;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.word.dto.ExampleSentenceDto;
import com.aica.aivoca.word.dto.MeaningDto;
import com.aica.aivoca.word.dto.WordGetResponseDto;
import com.aica.aivoca.word.external.AiDictionaryClient;
import com.aica.aivoca.word.external.dto.AiExample;
import com.aica.aivoca.word.external.dto.AiMeaning;
import com.aica.aivoca.word.external.dto.AiWordResponse;
import com.aica.aivoca.word.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicWordService {

    private final WordRepository wordRepository;
    private final MeaningRepository meaningRepository;
    private final PartOfSpeechRepository partOfSpeechRepository;
    private final MeaningPartOfSpeechRepository mpsRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;
    private final AiDictionaryClient aiClient;

    // 영어 품사 키 → 한글 품사 ID 매핑
    private static final Map<String, Long> POS_ID_MAP = Map.ofEntries(
            Map.entry("명사", 1L),
            Map.entry("동사", 2L),
            Map.entry("형용사", 3L),
            Map.entry("부사", 4L),
            Map.entry("대명사", 5L),
            Map.entry("전치사", 6L),
            Map.entry("접속사", 7L),
            Map.entry("감탄사", 8L)
    );

    @Transactional
    public SuccessStatusResponse<WordGetResponseDto> lookupAndSaveWordIfNeeded(String wordText) {
        boolean isNew = false;
        Word word = wordRepository.findByWordIgnoreCase(wordText).orElse(null);

        if (word == null) {
            isNew = true;
            AiWordResponse aiResponse = aiClient.getWordInfo(wordText);
            word = wordRepository.save(new Word(null, wordText));

            for (AiMeaning aiMeaning : aiResponse.meanings()) {
                Meaning meaning = meaningRepository.save(new Meaning(null, word, aiMeaning.meaning()));

                for (String posKey : aiMeaning.partOfSpeech()) {
                    Long posId = POS_ID_MAP.get(posKey.toLowerCase());
                    if (posId == null) continue;

                    PartOfSpeech pos = partOfSpeechRepository.findById(posId)
                            .orElseThrow(() -> new RuntimeException("Invalid POS id: " + posId));
                    mpsRepository.save(new MeaningPartOfSpeech(null, meaning, pos));
                }

                for (AiExample example : aiMeaning.exampleSentences()) {
                    exampleSentenceRepository.save(new ExampleSentence(null, meaning,
                            example.sentence(), example.meaning()));
                }
            }
        }

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

        WordGetResponseDto responseDto = new WordGetResponseDto(word.getId(), word.getWord(), meaningDtos);
        SuccessMessage message = isNew
                ? SuccessMessage.WORD_SAVED_FROM_AI
                : SuccessMessage.WORD_FOUND_IN_DB;

        return SuccessStatusResponse.of(message, responseDto);
    }
}
