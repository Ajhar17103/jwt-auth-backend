package com.example.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private String name;
    private String email;
    private String role;
    private boolean isActive;
    private int isDeleted;
}
