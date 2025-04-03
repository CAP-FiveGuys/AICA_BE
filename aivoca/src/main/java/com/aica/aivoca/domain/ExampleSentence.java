package com.aica.aivoca.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "example_sentence")
public class ExampleSentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "example_id")
    private Long exampleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mean_id", nullable = false)
    private Meaning meaning;

    @Column(name = "exam_sentence", columnDefinition = "TEXT", nullable = false)
    private String examSentence;

    @Column(name = "exam_meaning", columnDefinition = "TEXT", nullable = false)
    private String examMeaning;

    @Builder
    public ExampleSentence(Long exampleId, Meaning meaning, String examSentence, String examMeaning) {
        this.exampleId = exampleId;
        this.meaning = meaning;
        this.examSentence = examSentence;
        this.examMeaning = examMeaning;
    }
}