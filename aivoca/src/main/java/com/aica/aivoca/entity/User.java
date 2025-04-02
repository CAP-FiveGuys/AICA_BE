package com.aica.aivoca.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    @Builder
    public User(Long userId, String password, String email) {
        this.userId = userId;
        this.password = password;
        this.email = email;
    }
}