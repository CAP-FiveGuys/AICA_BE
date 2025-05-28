package com.aica.aivoca.user.service;

import com.aica.aivoca.domain.Users;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.sentence.repository.SentenceRepository;
import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.repository.SentenceWordRepository;
import com.aica.aivoca.user.repository.UsersRepository;
import com.aica.aivoca.word.repository.VocabularyListRepository;
import com.aica.aivoca.word.repository.VocabularyListWordRepository;
import com.aica.aivoca.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository userRepository;
    private final VocabularyListRepository vocabularyListRepository;
    private final SentenceRepository sentenceRepository;
    private final SentenceWordRepository sentenceWordRepository;
    private final VocabularyListWordRepository vocabularyListWordRepository;

    @Transactional(readOnly = true)
    public UsersInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_ID_NOT_FOUND));

        return new UsersInfoResponse(user.getId(), user.getUserId(), user.getEmail(), user.getUserNickname());
    }
    @Transactional
    public void deleteUser(Long userId) {

        // 0. 단어장-단어 연결 테이블 먼저 삭제
        vocabularyListWordRepository.deleteByVocabularyList_UserId(userId);

        // 1. 문장-단어 연결 먼저 삭제
        sentenceWordRepository.deleteByUserId(userId);

        // 2. 사용자 문장 삭제
        sentenceRepository.deleteByUserId(userId);

        // 3. 사용자 단어장 삭제
        vocabularyListRepository.deleteByUsers_Id(userId);

        // 4. 사용자 계정 삭제
        userRepository.deleteById(userId);
    }
}
