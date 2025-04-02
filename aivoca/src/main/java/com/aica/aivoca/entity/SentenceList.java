package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sentence_list")
public class SentenceList {
    @Id
    @Column(name = "sentence_list")
    private Long sentenceList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public SentenceList(Long sentenceList, User user) {
        this.sentenceList = sentenceList;
        this.user = user;
    }
}