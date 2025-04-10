package com.aica.aivoca.sentence.repository;

import com.aica.aivoca.domain.Sentence;
import com.aica.aivoca.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {

    boolean existsByIdAndUser(Long id, Users user);
    boolean existsByUserAndSentence(Users user, String sentence);
}