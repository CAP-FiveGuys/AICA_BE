package com.aica.aivoca.word.service;

import com.aica.aivoca.domain.*;
import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.user.repository.UsersRepository;
import com.aica.aivoca.word.dto.WordAddRequestDto;
import com.aica.aivoca.word.dto.WordResponseDto;
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

    @Transactional
    public SuccessStatusResponse<WordResponseDto> addWordToVocabulary(WordAddRequestDto requestDto) {
            // ✅ 1. 유저 조회
            Users user = usersRepository.findById(requestDto.userId())
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            // ✅ 2. 단어 조회
            Word word = wordRepository.findById(requestDto.wordId())
                    .orElseThrow(() -> new CustomException(ErrorMessage.WORD_NOT_FOUND));

            // ✅ 3. 단어장 조회 or 생성
            VocabularyList vocaList = vocabularyListRepository.findByUsers(user)
                    .orElseGet(() -> {
                        VocabularyList newList = VocabularyList.builder()
                                .users(user)
                                .build();
                        em.persist(newList); // JPA가 관리하는 상태로 저장
                        return newList;
                    });

            // ✅ 4. 중복 검사
            if (vocabularyListWordRepository.existsByVocabularyListAndWord(vocaList, word)) {
                throw new CustomException(ErrorMessage.WORD_ALREADY_IN_VOCABULARY);
            }

            // ✅ 5. 단어장-단어 연결 저장
            VocabularyListWord link = VocabularyListWord.builder()
                    .vocabularyList(vocaList)
                    .word(word)
                    .build();
            vocabularyListWordRepository.save(link);

            // ✅ 6. 의미/품사/예문 조회 및 응답 생성
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
}
