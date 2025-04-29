package com.aica.aivoca.user.service;

import com.aica.aivoca.domain.Users;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository userRepository;

    @Transactional(readOnly = true)
    public UsersInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_ID_NOT_FOUND));

        return new UsersInfoResponse(user.getId(), user.getUserId(), user.getEmail(), user.getUserNickname());
    }
}
