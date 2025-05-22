package com.example.jwt.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import com.example.jwt.dto.*;
import com.example.jwt.exception.DatabaseException;
import com.example.jwt.exception.EmailAlreadyExistsException;
import com.example.jwt.exception.InactiveUserException;
import com.example.jwt.exception.InvalidCredentialsException;
import com.example.jwt.mapper.LoginMapper;
import com.example.jwt.mapper.RegisterMapper;
import com.example.jwt.params.LogoutRequestParams;
import com.example.jwt.params.RefreshTokenRequestParams;
import com.example.jwt.entity.BlacklistedToken;
import com.example.jwt.entity.Users;
import com.example.jwt.params.LoginRequestParams;
import com.example.jwt.params.RegisterRequestParams;
import com.example.jwt.repository.BlacklistedTokenRepository;
import com.example.jwt.repository.UsersRepo;
import com.example.jwt.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {


    private final UsersRepo usersRepo;

    private final JWTUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final RegisterMapper registerMapper;

    private  final LoginMapper loginMapper;

    private final JobLauncher jobLauncher;
    private final Job userImportJob;

    @Autowired
    public AuthService(UsersRepo usersRepo, JWTUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, BlacklistedTokenRepository blacklistedTokenRepository, RegisterMapper registerMapper, LoginMapper loginMapper, JobLauncher jobLauncher, Job userImportJob) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.registerMapper = registerMapper;
        this.loginMapper = loginMapper;
        this.jobLauncher = jobLauncher;
        this.userImportJob = userImportJob;
    }

    public ApiResponse<RegisterResponseDto> register(RegisterRequestParams registrationRequest) {
        Optional<Users> existingUser = usersRepo.findByEmail(registrationRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        Users user = registerMapper.toUser(registrationRequest);
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setActive(true);
        user.setIs_deleted(0);

        Users savedUser = usersRepo.save(user);

        if (savedUser.getId() <= 0) {
            throw new DatabaseException("User could not be saved");
        }

        RegisterResponseDto responseDto = registerMapper.toResponseDto(savedUser);

        return ApiResponse.<RegisterResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Saved Successfully")
                .data(responseDto)
                .build();
    }

    public ApiResponse<LoginResponseDto> login(LoginRequestParams loginRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        Users user = usersRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getActive()) {
            throw new InactiveUserException("Your account is deactivated. Please contact support.");
        }

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        LoginResponseDto responseData = loginMapper.toResponseDto(user);
        responseData.setToken(accessToken);
        responseData.setRefreshToken(refreshToken);

        return ApiResponse.<LoginResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User login successful")
                .data(responseData)
                .build();
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

    public ApiResponse<BulkRegisterResponseDto> bulkRegister(MultipartFile file) throws IOException, JobExecutionException {
        String tempFilePath = System.getProperty("java.io.tmpdir") + "/bulk_users_" + System.currentTimeMillis() + ".xlsx";
        Files.copy(file.getInputStream(), Paths.get(tempFilePath));

        JobParameters parameters = new JobParametersBuilder()
                .addString("filePath", tempFilePath)
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(userImportJob, parameters);

        while (execution.isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for batch job to complete");
            }
        }

        ExecutionContext context = execution.getExecutionContext();
        int success = context.getInt("successCount", 0);
        int failure = context.getInt("failureCount", 0);

        return ApiResponse.<BulkRegisterResponseDto>builder()
                .statusCode(200)
                .message("Batch job completed.")
                .data(BulkRegisterResponseDto.builder()
                        .successCount(success)
                        .failureCount(failure)
                        .build())
                .build();
    }

}
