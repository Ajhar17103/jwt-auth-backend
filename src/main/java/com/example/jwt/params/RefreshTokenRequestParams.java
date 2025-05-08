package com.example.jwt.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestParams {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
