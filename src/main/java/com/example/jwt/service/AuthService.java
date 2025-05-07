package com.example.jwt.service;

import com.example.jwt.dto.Response;
import com.example.jwt.entity.BlacklistedToken;
import com.example.jwt.entity.Users;
import com.example.jwt.params.Request;
import com.example.jwt.repository.BlacklistedTokenRepository;
import com.example.jwt.repository.UsersRepo;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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


    public Response register(Request registrationRequest) {

        try{
            Optional <Users> existingUser = usersRepo.findByEmail(registrationRequest.getEmail());
            if (existingUser.isPresent()) {
                return Response.builder()
                        .statusCode(409)
                        .message("Email already exists!")
                        .build();
            }
            Users user = new Users();

           user.setEmail(registrationRequest.getEmail());
           user.setName(registrationRequest.getName());
           user.setRole(registrationRequest.getRole());
           user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

           Users userResult = usersRepo.save(user);

            if (userResult.getId() > 0) {
                System.out.println(userResult);
                return Response.builder()
                        .statusCode(200)
                        .message("User Saved Successfully")
                        .users(userResult)
                        .build();
            } else {
                return Response.builder()
                        .statusCode(500)
                        .message("User could not be saved")
                        .build();
            }

        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message("Exception occurred: " + e.getMessage())
                    .build();
        }
    }

    public Response login(Request loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            return Response.builder()
                    .statusCode(200)
                    .token(jwt)
                    .refreshToken(refreshToken)
                    .role(user.getRole())
                    .message("User Login Successfully")
                    .build();

        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message(e.getMessage())
                    .build();
        }
    }

    public Response refreshToken(Response refreshTokenRequest) {
        try {
            String email = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            Users user = usersRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), user)) {
                String newAccessToken = jwtUtils.generateToken(user);
                String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

                return Response.builder()
                        .statusCode(200)
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .role(user.getRole())
                        .message("Successfully Refreshed Token")
                        .build();
            } else {
                return Response.builder()
                        .statusCode(401)
                        .message("Invalid or expired refresh token.")
                        .build();
            }

        } catch (ExpiredJwtException e) {
            return Response.builder()
                    .statusCode(401)
                    .message("Refresh token has expired. Please log in again.")
                    .build();
        } catch (Exception e) {
            return Response.builder()
                    .statusCode(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    public Response logout(Response logoutRequest) {

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

                return Response.builder()
                                .statusCode(200)
                                .message("Successfully Logout!")
                                .build();
            }else{
                return Response.builder()
                        .statusCode(401)
                        .message("You Already logged Out!")
                        .build();
            }

        } catch (Exception e) {
            return Response.builder()
                    .statusCode(401)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }
}
