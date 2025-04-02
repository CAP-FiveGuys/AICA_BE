package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "part_of_speech")
public class PartOfSpeech {
    @Id
    @Column(name = "part_id")
    private Long partId;

    @Column(name = "part", nullable = false, length = 20)
    private String part;

    @Builder
    public PartOfSpeech(Long partId, String part) {
        this.partId = partId;
        this.part = part;
    }
}