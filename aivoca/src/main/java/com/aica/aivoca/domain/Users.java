package com.aica.aivoca.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_uid", nullable = false, unique = true, length = 50)
    private String userId; // 사용자가 입력하는 유저 아이디

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Builder
    public Users(Long id, String userId, String password, String email) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.email = email;
    }
}