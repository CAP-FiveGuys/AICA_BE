package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.PartOfSpeech;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartOfSpeechRepository extends JpaRepository<PartOfSpeech, Long> {
    boolean existsByName(String name);
    Optional<PartOfSpeech> findByName(String name);
}