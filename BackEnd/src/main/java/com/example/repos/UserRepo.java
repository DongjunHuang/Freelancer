package com.example.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByUsername(String username);
}
