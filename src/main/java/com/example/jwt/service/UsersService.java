package com.example.jwt.service;

import com.example.jwt.dto.Response;
import com.example.jwt.entity.Users;
import com.example.jwt.params.Request;
import com.example.jwt.repository.UsersRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService {


    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;


    public UsersService(UsersRepo usersRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Response getAllUser() {
        try {
            List<Users> users = usersRepo.findAll();

            return Response.builder()
                    .statusCode(200)
                    .message("All Users Successfully")
                    .usersList(users)
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public Response userDetails(int id) {
        try {
            Optional<Users> users = usersRepo.findById(id);
            if (users.isPresent()) {
                return Response.builder()
                        .statusCode(200)
                        .users(users.get())
                        .message("User found")
                        .build();
            } else {
                return Response.builder()
                        .statusCode(404)
                        .message("User not found")
                        .build();
            }
        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public Response updateUser(int id, Request users) {
        System.out.println(id);
        try {
            Optional<Users> user = usersRepo.findById(id);

            if (user.isEmpty()) {
                return Response.builder()
                        .statusCode(404)
                        .message("User Not Found!")
                        .build();
            }

            Users userToUpdate = user.get();

            // Check if new email is used by another user
            Optional<Users> userWithEmail = usersRepo.findByEmail(users.getEmail());
            if (userWithEmail.isPresent() && userWithEmail.get().getId() != userToUpdate.getId()) {
                return Response.builder()
                        .statusCode(409)
                        .message("Email already in use by another user.")
                        .build();
            }

            // Update fields
            userToUpdate.setName(users.getName());
            userToUpdate.setPassword(passwordEncoder.encode(users.getPassword()));
            userToUpdate.setRole(users.getRole());

            Users updatedUser = usersRepo.save(userToUpdate);

            return Response.builder()
                    .statusCode(200)
                    .message("User updated successfully")
                    .users(updatedUser)
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }
}
