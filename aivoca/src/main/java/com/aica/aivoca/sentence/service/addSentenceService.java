package com.aica.aivoca.sentence.service;

import com.aica.aivoca.domain.Sentence;
import com.aica.aivoca.domain.Users;
import com.aica.aivoca.global.exception.CustomException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.sentence.dto.SentenceRequestDto;
import com.aica.aivoca.sentence.dto.SentenceResponseDto;
import com.aica.aivoca.sentence.repository.SentenceRepository;
import com.aica.aivoca.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class addSentenceService {

    private final SentenceRepository sentenceRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public SuccessStatusResponse<SentenceResponseDto> addSentence(SentenceRequestDto requestDto) {
        if (requestDto.userId() == null) {
            throw new CustomException(ErrorMessage.USER_ID_REQUIRED);
        }

        if (requestDto.sentenceId() == null) {
            throw new CustomException(ErrorMessage.SENTENCE_ID_REQUIRED);
        }

        if (requestDto.sentence() == null || requestDto.sentence().trim().isEmpty()) {
            throw new CustomException(ErrorMessage.SENTENCE_TEXT_REQUIRED);
        }

        Users user = usersRepository.findById(requestDto.userId())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        if (sentenceRepository.existsByUserAndSentence(user, requestDto.sentence())) {
            throw new CustomException(ErrorMessage.SENTENCE_ALREADY_EXISTS);
        }

        if (sentenceRepository.existsByIdAndUser(requestDto.sentenceId(), user)) {
            throw new CustomException(ErrorMessage.SENTENCE_ID_ALREADY_EXISTS);
        }

        Sentence sentence = Sentence.builder()
                .id(requestDto.sentenceId())
                .userId(user.getId())
                .user(user)
                .sentence(requestDto.sentence())
                .build();

        sentenceRepository.save(sentence);

        SentenceResponseDto responseDto = new SentenceResponseDto(user.getId(), sentence.getSentence());
        return SuccessStatusResponse.of(SuccessMessage.SENTENCE_ADD_SUCCESS, responseDto);
    }

}