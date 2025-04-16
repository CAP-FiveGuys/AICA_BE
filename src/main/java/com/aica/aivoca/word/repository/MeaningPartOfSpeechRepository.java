package com.aica.aivoca.word.repository;

import com.aica.aivoca.domain.Meaning;
import com.aica.aivoca.domain.MeaningPartOfSpeech;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeaningPartOfSpeechRepository extends JpaRepository<MeaningPartOfSpeech, Long> {
    List<MeaningPartOfSpeech> findAllByMeaning(Meaning meaning);
}