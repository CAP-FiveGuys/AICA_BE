package com.aica.aivoca.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class PartOfSpeech {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected PartOfSpeech() {}

    public PartOfSpeech(String name) {
        this.name = name;
    }
}