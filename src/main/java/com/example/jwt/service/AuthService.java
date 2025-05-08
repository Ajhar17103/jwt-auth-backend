package com.example.jwt.service;

import com.example.jwt.dto.*;
import com.example.jwt.params.LogoutRequestParams;
import com.example.jwt.params.RefreshTokenRequestParams;
import com.example.jwt.entity.BlacklistedToken;
import com.example.jwt.entity.Users;
import com.example.jwt.params.LoginRequestParams;
import com.example.jwt.params.RegisterRequestParams;
import com.example.jwt.repository.BlacklistedTokenRepository;
import com.example.jwt.repository.UsersRepo;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Service
public class AuthService {


    private final UsersRepo usersRepo;

    private final JWTUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public AuthService(UsersRepo usersRepo, JWTUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }


    public ApiResponse<RegisterResponseDto> register(RegisterRequestParams registrationRequest) {

        try{
            Optional <Users> existingUser = usersRepo.findByEmail(registrationRequest.getEmail());
            if (existingUser.isPresent()) {
                return ApiResponse.<RegisterResponseDto>builder()
                        .statusCode(409)
                        .message("Email already exists!")
                        .build();
            }
            Users user = new Users();

           user.setEmail(registrationRequest.getEmail());
           user.setName(registrationRequest.getName());
           user.setRole(registrationRequest.getRole());
           user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
           user.setActive(true);
           user.setIsDeleted(0);
           Users userResult = usersRepo.save(user);

            if (userResult.getId() > 0) {
                System.out.println(userResult);
                RegisterResponseDto registerRes = RegisterResponseDto.builder()
                        .name(userResult.getName())
                        .email(userResult.getEmail())
                        .role(userResult.getRole())
                        .isActive(userResult.isActive())
                        .isDeleted(userResult.getIsDeleted())
                        .build();

                return ApiResponse.<RegisterResponseDto>builder()
                        .statusCode(200)
                        .message("User Saved Successfully")
                        .data(registerRes)
                        .build();
            } else {
                return ApiResponse.<RegisterResponseDto>builder()
                        .statusCode(500)
                        .message("User could not be saved")
                        .build();
            }

        } catch (Exception e) {
            return ApiResponse.<RegisterResponseDto>builder()
                    .statusCode(500)
                    .message("Exception occurred: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<LoginResponseDto> login(LoginRequestParams loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            Users user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();

            String jwt = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            LoginResponseDto responseData = LoginResponseDto.builder()
                    .token(jwt)
                    .refreshToken(refreshToken)
                    .role(user.getRole())
                    .isActive(user.isActive())
                    .isDeleted(user.getIsDeleted())
                    .build();

            return ApiResponse.<LoginResponseDto>builder()
                    .statusCode(200)
                    .message("User login successful")
                    .data(responseData)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<LoginResponseDto>builder()
                    .statusCode(500)
                    .message("Login failed: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<TokenResponseDto> refreshToken(RefreshTokenRequestParams refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            String email = jwtUtils.extractUsername(refreshToken);
            Users user = usersRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            if (jwtUtils.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtUtils.generateToken(user);
                String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

                TokenResponseDto tokenResponse = TokenResponseDto.builder()
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .role(user.getRole())
                        .build();

                return ApiResponse.<TokenResponseDto>builder()
                        .statusCode(200)
                        .message("Successfully Refreshed Token")
                        .data(tokenResponse)
                        .build();
            } else {
                return ApiResponse.<TokenResponseDto>builder()
                        .statusCode(401)
                        .message("Invalid or expired refresh token.")
                        .build();
            }

        } catch (ExpiredJwtException e) {
            return ApiResponse.<TokenResponseDto>builder()
                    .statusCode(401)
                    .message("Refresh token has expired. Please log in again.")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<TokenResponseDto>builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }


    public ApiResponse<Void> logout(LogoutRequestParams logoutRequest) {

        try{
            String token = logoutRequest.getToken();
            String refreshToken = logoutRequest.getRefreshToken();

            if(!blacklistedTokenRepository.existsByToken(token) && !blacklistedTokenRepository.existsByToken(refreshToken)){
                Date tokenExp = jwtUtils.extractExpiration(token);
                BlacklistedToken access = new BlacklistedToken();
                access.setType("access token");
                access.setToken(token);
                access.setExpiration(tokenExp);
                blacklistedTokenRepository.save(access);

                Date refreshExp = jwtUtils.extractExpiration(refreshToken);
                BlacklistedToken refresh = new BlacklistedToken();
                refresh.setType("refresh token");
                refresh.setToken(refreshToken);
                refresh.setExpiration(refreshExp);
                blacklistedTokenRepository.save(refresh);

                return ApiResponse.<Void>builder()
                                .statusCode(200)
                                .message("Successfully Logout!")
                                .build();
            }else{
                return ApiResponse.<Void>builder()
                        .statusCode(401)
                        .message("You Already logged Out!")
                        .build();
            }

        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .statusCode(401)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }
}
