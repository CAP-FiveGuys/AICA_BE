package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meaning_partofspeech")
public class MeaningPartOfSpeech {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meaning_partofspeech_id")
    private Long meaningPartOfSpeechId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mean_id", nullable = false)
    private Meaning meaning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private PartOfSpeech partOfSpeech;

    @Builder
    public MeaningPartOfSpeech(Long meaningPartOfSpeechId, Meaning meaning, PartOfSpeech partOfSpeech) {
        this.meaningPartOfSpeechId = meaningPartOfSpeechId;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
    }
}