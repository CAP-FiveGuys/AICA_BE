package com.aica.aivoca.word.service;

import com.aica.aivoca.domain.*;
import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.user.repository.UsersRepository;
import com.aica.aivoca.word.dto.*;
import com.aica.aivoca.word.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {

    private final UsersRepository usersRepository;
    private final WordRepository wordRepository;
    private final VocabularyListRepository vocabularyListRepository;
    private final VocabularyListWordRepository vocabularyListWordRepository;
    private final MeaningRepository meaningRepository;
    private final MeaningPartOfSpeechRepository mpsRepository;
    private final ExampleSentenceRepository exampleSentenceRepository;

    @PersistenceContext
    private EntityManager em;

    // ✅ 단어장에 단어 추가
    @Transactional
    public SuccessStatusResponse<WordResponseDto> addWordToVocabulary(WordAddRequestDto requestDto, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Word word = wordRepository.findById(requestDto.wordId())
                .orElseThrow(() -> new CustomException(ErrorMessage.WORD_NOT_FOUND));

        VocabularyList vocaList = vocabularyListRepository.findByUsers(user)
                .orElseGet(() -> {
                    VocabularyList newList = VocabularyList.builder()
                            .users(user)
                            .build();
                    em.persist(newList);
                    return newList;
                });

        if (vocabularyListWordRepository.existsByVocabularyListAndWord(vocaList, word)) {
            throw new CustomException(ErrorMessage.WORD_ALREADY_IN_VOCABULARY);
        }

        VocabularyListWord link = VocabularyListWord.builder()
                .vocabularyList(vocaList)
                .word(word)
                .build();
        vocabularyListWordRepository.save(link);

        List<Meaning> meanings = meaningRepository.findAllByWord(word);

        WordResponseDto responseDto = WordResponseDto.from(
                user.getId(),
                word.getWord(),
                meanings,
                mpsRepository,
                exampleSentenceRepository
        );

        return SuccessStatusResponse.of(SuccessMessage.WORD_ADDED_TO_VOCABULARY, responseDto);
    }

    // ✅ 단어장 전체 단어 조회
    @Transactional(readOnly = true)
    public SuccessStatusResponse<List<WordGetResponseDto>> getMyVocabularyWords(Long userId) {
        VocabularyList vocaList = vocabularyListRepository.findByUsers_Id(userId)
                .orElseThrow(() -> new CustomException(ErrorMessage.VOCABULARY_LIST_NOT_FOUND));

        List<VocabularyListWord> vocabWords = vocabularyListWordRepository.findByVocabularyList(vocaList);

        List<WordGetResponseDto> result = vocabWords.stream()
                .map(vocabWord -> {
                    Word word = vocabWord.getWord();
                    List<Meaning> meanings = meaningRepository.findAllByWord(word);

                    List<MeaningDto> meaningDtos = meanings.stream()
                            .map(meaning -> {
                                List<String> parts = mpsRepository.findAllByMeaning(meaning).stream()
                                        .map(mp -> mp.getPartOfSpeech().getName())
                                        .toList();

                                List<ExampleSentenceDto> examples = exampleSentenceRepository.findAllByMeaning(meaning).stream()
                                        .map(ex -> new ExampleSentenceDto(
                                                ex.getExamSentence(),  // ✅ 수정: examSentence
                                                ex.getExamMeaning()    // ✅ 수정: examMeaning
                                        ))
                                        .toList();

                                return new MeaningDto(meaning.getMean(), parts, examples);
                            })
                            .toList();

                    return new WordGetResponseDto(
                            word.getId(),
                            word.getWord(),
                            meaningDtos
                    );
                })
                .toList();

        return SuccessStatusResponse.of(SuccessMessage.GET_WORD_SUCCESS, result);
    }
}
