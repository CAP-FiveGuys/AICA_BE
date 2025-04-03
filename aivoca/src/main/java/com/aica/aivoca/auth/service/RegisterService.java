package com.aica.aivoca.auth.service;

import com.aica.aivoca.auth.dto.UserRegisterRequestDto;
import com.aica.aivoca.auth.repository.EmailVerificationRepository;
import com.aica.aivoca.domain.Users;
import com.aica.aivoca.user.repository.UsersRepository;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(UserRegisterRequestDto request) {
        // 이메일 인증 여부 확인
        if (!emailVerificationRepository.isNotVerified(request.email())) {
            throw new BusinessException(ErrorMessage.EMAIL_NOT_VERIFIED);
        }

        // 이메일 중복 검사
        if (usersRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorMessage.DUPLICATED_EMAIL);
        }

        // 유저 ID 중복 검사
        if (usersRepository.existsByUserId(request.userId())) {
            throw new BusinessException(ErrorMessage.DUPLICATED_USER_ID);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 유저 저장
        Users user = Users.builder()
                .userId(request.userId())
                .email(request.email())
                .password(encodedPassword)
                .build();

        usersRepository.save(user);

        // 인증 기록 삭제
        emailVerificationRepository.deleteVerification(request.email());
    }
}
