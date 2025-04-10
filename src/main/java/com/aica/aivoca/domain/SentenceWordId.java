package com.aica.aivoca.domain;

import java.io.Serializable;
import java.util.Objects;

public class SentenceWordId implements Serializable {

    private Long sentenceId;
    private Long userId;
    private Long wordId;

    public SentenceWordId() {}

    public SentenceWordId(Long sentenceId, Long userId, Long wordId) {
        this.sentenceId = sentenceId;
        this.userId = userId;
        this.wordId = wordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SentenceWordId that)) return false;
        return Objects.equals(sentenceId, that.sentenceId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceId, userId, wordId);
    }
}