package com.aica.aivoca.user.repository;

import com.aica.aivoca.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    // 로그인 시 ID로 유저 조회
    Optional<Users> findByUserId(String userId);

    // 아이디 중복 체크
    boolean existsByUserId(String userId);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // Long 타입의 id로 유저 존재 여부 확인
    boolean existsById(Long id);
}