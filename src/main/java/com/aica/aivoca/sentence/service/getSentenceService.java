package com.aica.aivoca.sentence.service;

import com.aica.aivoca.domain.Sentence;
import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.NotFoundException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.sentence.dto.SentenceGetResponseDto;
import com.aica.aivoca.sentence.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class getSentenceService {

    private final SentenceRepository sentenceRepository;

    @Transactional(readOnly = true)
    public SuccessStatusResponse<List<SentenceGetResponseDto>> getSentences(Long userId, String search) {
        List<Sentence> sentences;

        if (search != null && !search.isEmpty()) {
            sentences = sentenceRepository.findByUser_IdAndSentenceContaining(userId, search);
        } else {
            sentences = sentenceRepository.findByUser_Id(userId);
        }


        if (sentences.isEmpty()) {
            throw new NotFoundException(ErrorMessage.SENTENCE_NOT_FOUND_BY_USER);
        }

        List<SentenceGetResponseDto> result = sentences.stream()
                .map(s -> {
                    Long sentenceOwnerId = s.getUser() != null ? s.getUser().getId() : null;
                    return new SentenceGetResponseDto(
                            s.getId(),
                            sentenceOwnerId,
                            s.getSentence()
                    );
                })
                .toList();
        return SuccessStatusResponse.of(SuccessMessage.SENTENCE_GET_SUCCESS, result);


    }
}