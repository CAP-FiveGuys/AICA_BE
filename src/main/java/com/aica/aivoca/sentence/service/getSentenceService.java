package com.aica.aivoca.sentence.service;

import com.aica.aivoca.domain.Sentence;
import com.aica.aivoca.global.exception.NotFoundException;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.sentence.dto.SentenceGetResponseDto;
import com.aica.aivoca.sentence.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class getSentenceService {

    private final SentenceRepository sentenceRepository;

    @Transactional(readOnly = true)
    public SuccessStatusResponse<List<SentenceGetResponseDto>> getSentences(Long ignoredUserId, String search) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.userId();

        List<Sentence> sentences = (search != null && !search.isEmpty())
                ? sentenceRepository.findByUser_IdAndSentenceContaining(userId, search)
                : sentenceRepository.findByUser_Id(userId);

        if (sentences.isEmpty()) {
            throw new NotFoundException(ErrorMessage.SENTENCE_NOT_FOUND_BY_USER);
        }

        List<SentenceGetResponseDto> result = sentences.stream()
                .map(s -> new SentenceGetResponseDto(
                        s.getId(),
                        s.getUser() != null ? s.getUser().getId() : null,
                        s.getSentence()))
                .toList();

        return SuccessStatusResponse.of(SuccessMessage.SENTENCE_GET_SUCCESS, result);
    }
}