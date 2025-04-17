package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.VocabularyListWord;
import com.aica.aivoca.domain.VocabularyList;
import com.aica.aivoca.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocabularyListWordRepository extends JpaRepository<VocabularyListWord, Long> {

    boolean existsByVocabularyListAndWord(VocabularyList vocabularyList, Word word);

    Optional<VocabularyListWord> findByVocabularyListAndWord(VocabularyList vocabularyList, Word word);
}