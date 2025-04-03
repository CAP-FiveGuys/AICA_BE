package com.aica.aivoca.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vocabulary_list")
public class VocabularyList {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private Users users;

    @Builder
    public VocabularyList(Users users) {
        this.users = users;
        this.userId = users.getId(); // 또는 Users 클래스의 실제 getter 이름 사용
    }
}