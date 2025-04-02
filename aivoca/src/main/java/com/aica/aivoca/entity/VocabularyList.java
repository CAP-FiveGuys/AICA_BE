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

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Builder
    public VocabularyList(Long vocaListId, Users users) {
        this.vocaListId = vocaListId;
        this.users = users;
    }
}