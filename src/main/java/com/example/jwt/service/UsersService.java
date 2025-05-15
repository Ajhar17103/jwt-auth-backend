package com.example.jwt.service;

import com.example.jwt.dto.ApiResponse;
import com.example.jwt.dto.UserResponseDto;
import com.example.jwt.entity.Users;
import com.example.jwt.mapper.UserDetailsMapper;
import com.example.jwt.params.UserUpdateRequestParams;
import com.example.jwt.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsersService {

    private final UsersRepo usersRepo;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsMapper userDetailsMapper;


    @Autowired
    public UsersService(UsersRepo usersRepo, PasswordEncoder passwordEncoder, UserDetailsMapper userDetailsMapper) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsMapper = userDetailsMapper;
    }

    public ApiResponse<List<UserResponseDto>> getAllUser() {
        try {
            List<Users> users = usersRepo.findAll();

            List<UserResponseDto> responseData = userDetailsMapper.toResponseListDto(users).stream()
                    .filter(user -> user.getIs_deleted() == 0)
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

                if (userInfo.getIs_deleted() == 1) {
                    return ApiResponse.<UserResponseDto>builder()
                            .statusCode(400)
                            .message("User is deleted and cannot be updated")
                            .build();
                }

                UserResponseDto responseData = userDetailsMapper.toResponseDto(userInfo);

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

            if (userToUpdate.getIs_deleted() == 1) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(400)
                        .message("User is deleted and cannot be updated")
                        .build();
            }
            userToUpdate.setName(users.getName());
            userToUpdate.setRole(users.getRole());
            userToUpdate.setActive(users.isActive());

            if (users.getPassword() != null && !users.getPassword().isBlank()) {
                userToUpdate.setPassword(passwordEncoder.encode(users.getPassword()));
            }

            Users updatedUser = usersRepo.save(userToUpdate);

            UserResponseDto responseDto = userDetailsMapper.toResponseDto(updatedUser);

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

            if (user.getIs_deleted() == 1) {
                return ApiResponse.<UserResponseDto>builder()
                        .statusCode(409)
                        .message("User is already deleted")
                        .build();
            }

            user.setIs_deleted(1);
            user.setActive(false);
            Users deletedUser = usersRepo.save(user);

            UserResponseDto responseDto = userDetailsMapper.toResponseDto(deletedUser);

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
