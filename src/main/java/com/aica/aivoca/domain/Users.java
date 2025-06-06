package com.aica.aivoca.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Setter
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Setter
    @Column(name = "user_nickname", nullable = false, length = 100)
    private String userNickname;

    @Setter
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Builder
    public Users(Long id, String userId, String password,String userNickname, String email) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.userNickname = userNickname;
        this.email = email;
    }
}