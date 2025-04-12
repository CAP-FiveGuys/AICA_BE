package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.Meaning;
import com.aica.aivoca.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeaningRepository extends JpaRepository<Meaning, Long> {
    List<Meaning> findAllByWord(Word word);
}