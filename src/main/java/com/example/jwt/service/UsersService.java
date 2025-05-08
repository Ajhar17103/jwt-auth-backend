package com.example.jwt.service;

import com.example.jwt.dto.ApiResponse;
import com.example.jwt.dto.UserResponseDto;
import com.example.jwt.entity.Users;
import com.example.jwt.params.UserUpdateRequestParams;
import com.example.jwt.repository.UsersRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsersService {


    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;


    public UsersService(UsersRepo usersRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse<List<UserResponseDto>> getAllUser() {
        try {
            List<Users> users = usersRepo.findAll();


            List<UserResponseDto> responseData = users.stream()
                    .filter(user -> user.getIsDeleted() == 0)
                    .map(user -> UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(user.getRole())
                            .isActive(user.isActive())
                            .isDeleted(user.getIsDeleted())
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.<List<UserResponseDto>>builder()
                    .statusCode(200)
                    .data(responseData)
                    .message("All Users Retrieved Successfully")
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<UserResponseDto>>builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<UserResponseDto> userDetails(int id) {
        try {
            Optional<Users> users = usersRepo.findById(id);

            if (users.isPresent()) {
                Users userInfo = users.get();

                // Check if the user is marked as deleted
                if (userInfo.getIsDeleted() == 1) {
                    return ApiResponse.<UserResponseDto>builder()
                            .statusCode(400) // Bad request (invalid action)
                            .message("User is deleted and cannot be updated")
                            .build();
                }

                // Proceed if user is not deleted
                UserResponseDto responseData = UserResponseDto.builder()
                        .id(userInfo.getId())
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .role(userInfo.getRole())
                        .isActive(userInfo.isActive())
                        .isDeleted(userInfo.getIsDeleted())
                        .build();

                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(200)
                        .data(responseData)
                        .message("User found")
                        .build();
            } else {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(404)
                        .message("User not found")
                        .build();
            }
        } catch (Exception e) {
            return ApiResponse.<UserResponseDto>builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<UserResponseDto> updateUser(int id, UserUpdateRequestParams users) {
        try {
            Optional<Users> userOptional = usersRepo.findById(id);

            if (userOptional.isEmpty()) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(404)
                        .message("User not found")
                        .build();
            }

            Users userToUpdate = userOptional.get();

            // Check if the user is marked as deleted
            if (userToUpdate.getIsDeleted() == 1) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(400) // Bad request (invalid action)
                        .message("User is deleted and cannot be updated")
                        .build();
            }

            // Update fields if the user is not deleted
            userToUpdate.setName(users.getName());
            userToUpdate.setRole(users.getRole());
            userToUpdate.setActive(users.isActive());

            if (users.getPassword() != null && !users.getPassword().isBlank()) {
                userToUpdate.setPassword(passwordEncoder.encode(users.getPassword()));
            }

            Users updatedUser = usersRepo.save(userToUpdate);

            // Build response DTO
            UserResponseDto responseDto = UserResponseDto.builder()
                    .id(updatedUser.getId())
                    .email(updatedUser.getEmail())
                    .name(updatedUser.getName())
                    .role(updatedUser.getRole())
                    .isActive(updatedUser.isActive())
                    .isDeleted(updatedUser.getIsDeleted())
                    .build();

            return ApiResponse.<UserResponseDto>builder()
                    .statusCode(200)
                    .message("User updated successfully")
                    .data(responseDto)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<UserResponseDto>builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<UserResponseDto> deleteUser(int id) {
        try {
            Optional<Users> userOptional = usersRepo.findById(id);

            if (userOptional.isEmpty()) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(404)
                        .message("User not found")
                        .build();
            }

            Users user = userOptional.get();

            if (user.getIsDeleted() == 1) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(409)
                        .message("User is already deleted")
                        .build();
            }

            user.setIsDeleted(1);
            Users deletedUser = usersRepo.save(user);

            UserResponseDto responseDto = UserResponseDto.builder()
                    .id(deletedUser.getId())
                    .name(deletedUser.getName())
                    .email(deletedUser.getEmail())
                    .role(deletedUser.getRole())
                    .isActive(deletedUser.isActive())
                    .isDeleted(deletedUser.getIsDeleted())
                    .build();

            return ApiResponse.<UserResponseDto>builder()
                    .statusCode(200)
                    .message("User marked as deleted")
                    .data(responseDto)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<UserResponseDto>builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

}
