package com.aica.aivoca.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "word", uniqueConstraints = @UniqueConstraint(columnNames = "word"))
@Getter
@NoArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long id;

    @Column(name = "word", nullable = false, unique = true)
    private String word;

    @Builder
    public Word(Long id, String word) {
        this.id = id;
        this.word = word;
    }
}