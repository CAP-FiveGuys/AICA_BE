package com.aica.aivoca.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@IdClass(SentenceId.class)
@Table(name = "sentence")
@Getter
@NoArgsConstructor
public class Sentence {

    @Id
    @Column(name = "sentence_id")
    private Long id; // 사용자별 수동 삽입

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Users user;

    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    @Builder
    public Sentence(Long id, Long userId, Users user, String sentence) {
        this.id = id;
        this.userId = userId;
        this.user = user;
        this.sentence = sentence;
    }
}