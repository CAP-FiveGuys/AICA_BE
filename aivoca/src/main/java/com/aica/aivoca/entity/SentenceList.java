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
    @Column(name = "sentence_list_id")
    private Long sentenceListId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Builder
    public SentenceList(Long sentenceListId, Users users) {
        this.sentenceListId = sentenceListId;
        this.users = users;
    }
}