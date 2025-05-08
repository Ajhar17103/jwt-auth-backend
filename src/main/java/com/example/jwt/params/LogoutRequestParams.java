package com.example.jwt.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestParams {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
