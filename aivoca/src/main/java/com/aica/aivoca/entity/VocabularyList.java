package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vocabulary_list")
public class VocabularyList {
    @Id
    @Column(name = "voca_list_id")
    private Long vocaListId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public VocabularyList(Long vocaListId, User user) {
        this.vocaListId = vocaListId;
        this.user = user;
    }
}