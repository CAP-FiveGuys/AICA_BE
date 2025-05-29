package com.aica.aivoca.user.service;

import com.aica.aivoca.auth.repository.EmailVerificationRepository; // 👈 이 import 문은 여전히 필요합니다.
import com.aica.aivoca.domain.Users;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.sentence.repository.SentenceRepository;
import com.aica.aivoca.user.dto.UserUpdateRequestDto;
import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.repository.SentenceWordRepository;
import com.aica.aivoca.user.repository.UsersRepository;
import com.aica.aivoca.word.repository.VocabularyListRepository;
import com.aica.aivoca.word.repository.VocabularyListWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository userRepository;
    private final VocabularyListRepository vocabularyListRepository;
    private final SentenceRepository sentenceRepository;
    private final SentenceWordRepository sentenceWordRepository;
    private final VocabularyListWordRepository vocabularyListWordRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository; // 👈 이 필드를 추가해야 합니다!

    @Transactional(readOnly = true)
    public UsersInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_NOT_FOUND)); // USER_ID_NOT_FOUND는 USER_NOT_FOUND로 통합하는 게 좋습니다.

        return new UsersInfoResponse(user.getId(), user.getUserId(), user.getEmail(), user.getUserNickname());
    }


    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorMessage.USER_NOT_FOUND);
        }

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



    @Transactional
    public Map<String, String> updateUser(Long id, UserUpdateRequestDto requestDto) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_NOT_FOUND));

        Map<String, String> updatedInfo = new HashMap<>();

        boolean isPasswordUpdate = requestDto.currentPassword() != null ||
                requestDto.newPassword() != null ||
                requestDto.confirmNewPassword() != null;

        boolean isEmailUpdate = requestDto.newEmail() != null;

        if (isPasswordUpdate && isEmailUpdate) {
            throw new BusinessException(ErrorMessage.PASSWORD_EMAIL_SIMULTANEOUS_CHANGE_NOT_ALLOWED);
        }

        if (!isPasswordUpdate && !isEmailUpdate) {
            throw new BusinessException(ErrorMessage.NO_UPDATE_DATA_PROVIDED);
        }

        if (isPasswordUpdate) {
            updatePassword(user, requestDto, updatedInfo);
        } else if (isEmailUpdate) {
            updateEmail(user, requestDto, updatedInfo);
        }

        userRepository.save(user);
        return updatedInfo;
    }

    private void updatePassword(Users user, UserUpdateRequestDto requestDto, Map<String, String> updatedInfo) {
        if (requestDto.currentPassword() == null || requestDto.currentPassword().isEmpty() ||
                requestDto.newPassword() == null || requestDto.newPassword().isEmpty() ||
                requestDto.confirmNewPassword() == null || requestDto.confirmNewPassword().isEmpty()) {
            throw new BusinessException(ErrorMessage.PASSWORD_CHANGE_REQUIRED_FIELDS_MISSING);
        }

        if (!passwordEncoder.matches(requestDto.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorMessage.CURRENT_PASSWORD_MISMATCH);
        }

        if (!requestDto.newPassword().equals(requestDto.confirmNewPassword())) {
            throw new BusinessException(ErrorMessage.NEW_PASSWORD_CONFIRMATION_MISMATCH);
        }

        if (passwordEncoder.matches(requestDto.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorMessage.NEW_PASSWORD_SAME_AS_CURRENT);
        }

        user.setPassword(passwordEncoder.encode(requestDto.newPassword()));
    }

    private void updateEmail(Users user, UserUpdateRequestDto requestDto, Map<String, String> updatedInfo) {
        if (requestDto.newEmail() == null || requestDto.newEmail().isEmpty()) {
            throw new BusinessException(ErrorMessage.NEW_EMAIL_REQUIRED);
        }

        if (userRepository.existsByEmail(requestDto.newEmail())) {
            throw new BusinessException(ErrorMessage.DUPLICATED_EMAIL);
        }

        user.setEmail(requestDto.newEmail());
        updatedInfo.put("userEmail", user.getEmail());
    }
}