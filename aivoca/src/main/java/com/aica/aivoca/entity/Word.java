package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "word")
public class Word {
    @Id
    @Column(name = "word_id")
    private Long wordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voca_list_id", nullable = false)
    private VocabularyList vocabularyList;

    @Column(nullable = false, length = 255)
    private String word;

    @Builder
    public Word(Long wordId, VocabularyList vocabularyList, String word) {
        this.wordId = wordId;
        this.vocabularyList = vocabularyList;
        this.word = word;
    }
}