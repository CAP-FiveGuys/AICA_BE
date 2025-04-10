package com.aica.aivoca.user.repository;

import com.aica.aivoca.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);
}
