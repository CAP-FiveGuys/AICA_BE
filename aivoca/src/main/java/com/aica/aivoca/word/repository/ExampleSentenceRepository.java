package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.ExampleSentence;
import com.aica.aivoca.domain.Meaning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleSentenceRepository extends JpaRepository<ExampleSentence, Long> {
    List<ExampleSentence> findAllByMeaning(Meaning meaning);
}