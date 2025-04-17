package com.aica.aivoca.word.init;

import com.aica.aivoca.domain.PartOfSpeech;
import com.aica.aivoca.word.repository.PartOfSpeechRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartOfSpeechInitializer {

    private final PartOfSpeechRepository partOfSpeechRepository;

    @PostConstruct
    public void init() {
        insertIfNotExists("명사");
        insertIfNotExists("동사");
        insertIfNotExists("형용사");
        insertIfNotExists("부사");
        insertIfNotExists("대명사");
        insertIfNotExists("전치사");
        insertIfNotExists("접속사");
        insertIfNotExists("감탄사");
    }

    private void insertIfNotExists(String name) {
        if (!partOfSpeechRepository.existsByName(name)) {
            partOfSpeechRepository.save(new PartOfSpeech(name));
        }
    }
}
