package com.example.jwt.repository;

import com.example.jwt.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface UsersRepo extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findById(int id);

    @Query(value = "SELECT email FROM users", nativeQuery = true)
    Set<String> findAllEmails();
}
