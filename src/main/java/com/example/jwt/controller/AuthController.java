package com.example.jwt.controller;


import com.example.jwt.dto.*;
import com.example.jwt.params.LogoutRequestParams;
import com.example.jwt.params.RefreshTokenRequestParams;
import com.example.jwt.params.LoginRequestParams;
import com.example.jwt.params.RegisterRequestParams;
import com.example.jwt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> register(@Valid @RequestBody RegisterRequestParams registerRequest) {
        ApiResponse<RegisterResponseDto> registerRes = authService.register(registerRequest);
        return buildResponse(registerRes);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestParams loginRequest){
        ApiResponse <LoginResponseDto> response = authService.login(loginRequest);
        return buildResponse(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestParams request) {
        ApiResponse<TokenResponseDto> response = authService.refreshToken(request);
        return buildResponse(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequestParams req) {
        ApiResponse<Void> response=authService.logout(req);
        return buildResponse(response);
    }

}
