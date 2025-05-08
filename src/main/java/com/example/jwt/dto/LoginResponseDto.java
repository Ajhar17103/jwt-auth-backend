package com.example.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String refreshToken;
    private String role;
    private Boolean isActive;
    private int isDeleted;
}