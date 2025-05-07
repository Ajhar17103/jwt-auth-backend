package com.example.jwt.repository;

import com.example.jwt.entity.Users;
import com.example.jwt.params.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findById(Request id);
}
