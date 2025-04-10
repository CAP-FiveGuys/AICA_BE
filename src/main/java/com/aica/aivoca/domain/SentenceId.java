package com.aica.aivoca.domain;

import java.io.Serializable;
import java.util.Objects;

public class SentenceId implements Serializable {
    private Long id;
    private Long userId;

    public SentenceId() {}

    public SentenceId(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SentenceId that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}