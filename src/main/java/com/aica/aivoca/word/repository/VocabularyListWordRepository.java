package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.VocabularyListWord;
import com.aica.aivoca.domain.VocabularyList;
import com.aica.aivoca.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VocabularyListWordRepository extends JpaRepository<VocabularyListWord, Long> {

    boolean existsByVocabularyListAndWord(VocabularyList vocabularyList, Word word);

    Optional<VocabularyListWord> findByVocabularyListAndWord(VocabularyList vocabularyList, Word word);

    // ✅ 단어장에 등록된 모든 단어 조회
    List<VocabularyListWord> findByVocabularyList(VocabularyList vocabularyList);

    // ✅ 단어장 ID와 단어 ID로 단어 조회 (새로 추가)
    Optional<VocabularyListWord> findByVocabularyList_UserIdAndWord_Id(Long vocabularyListUserId, Long wordId);

    void deleteByVocabularyList_UserId(Long userId);
}
