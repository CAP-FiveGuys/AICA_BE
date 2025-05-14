package com.aica.aivoca.sentence.repository;

import com.aica.aivoca.domain.Sentence;
import com.aica.aivoca.domain.SentenceId;
import com.aica.aivoca.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, SentenceId> {


    boolean existsByIdAndUser(Long id, Users user);
    boolean existsByUserAndSentence(Users user, String sentence);
    List<Sentence> findByUser_Id(Long userId);
    List<Sentence> findByUser_IdAndSentenceContaining(Long userId, String sentence);

    // --- 복합 키 존재 여부 확인 메소드 추가 ---
    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);

}