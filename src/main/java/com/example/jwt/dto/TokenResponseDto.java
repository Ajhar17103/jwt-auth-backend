package com.example.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponseDto {
    private String token;
    private String refreshToken;
    private String role;
}
