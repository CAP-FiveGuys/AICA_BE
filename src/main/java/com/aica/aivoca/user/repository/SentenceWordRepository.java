package com.aica.aivoca.user.repository;

import com.aica.aivoca.domain.SentenceWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceWordRepository extends JpaRepository<SentenceWord, Long> {
    void deleteByUserId(Long userId);
}