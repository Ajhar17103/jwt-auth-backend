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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.example.jwt.utils.ExcelUtils.createSheet;
import static com.example.jwt.utils.ExcelUtils.getCellValue;

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

        String jwt = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        LoginResponseDto responseData = loginMapper.toResponseDto(user);
        responseData.setToken(jwt);
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

    public ApiResponse<BulkRegisterResponseDto> registerUsersInBatchAndSaveReport(MultipartFile file) {
        List<String[]> resultRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int batchSize = 1000;
            List<Users> batch = new ArrayList<>(batchSize);

            Set<String> existingEmails = usersRepo.findAllEmails();
            int successCount = 0;
            int failureCount = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String name = getCellValue(row.getCell(0));
                String email = getCellValue(row.getCell(1));
                String role = getCellValue(row.getCell(2));
                String password = getCellValue(row.getCell(3));

                try {
                    StringBuilder errorMessage = new StringBuilder();

//                    if (existingEmails.contains(email)) {
//                        failureCount++;
//                        errorMessage.append("Duplicate email; ");
//                    }
//
//                    if (!isValidPassword(password)) {
//                        failureCount++;
//                        errorMessage.append("Invalid password format; ");
//                    }
//
//                    if (!isValidEmail(email)) {
//                        failureCount++;
//                        errorMessage.append("Invalid email format; ");
//                    }
                    if (!errorMessage.isEmpty()) {
                        resultRows.add(new String[]{name, email, role, "Failed", errorMessage.toString()});
                        continue;
                    }
                    Users user = new Users();
                    user.setName(name);
                    user.setEmail(email);
                    user.setRole(role);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setActive(true);
                    user.setIs_deleted(0);

                    batch.add(user);
                    existingEmails.add(email);
                    resultRows.add(new String[]{name, email, role, "Success", ""});
                    successCount++;

                    if (batch.size() == batchSize) {
                        usersRepo.saveAll(batch);
                        batch.clear();
                    }

                } catch (Exception e) {
                    failureCount++;
                    resultRows.add(new String[]{name, email, role, "Failed", "Error: " + e.getMessage()});
                }
            }

            if (!batch.isEmpty()) {
                usersRepo.saveAll(batch);
            }

            Workbook resultWorkbook = new XSSFWorkbook();
           createSheet(resultWorkbook, "Registration Report",
                    new String[]{"Name", "Email", "Role", "Status", "Remarks"}, resultRows);

            String filename = "bulk-register-report-" + System.currentTimeMillis() + ".xlsx";
            Path path = Paths.get(System.getProperty("java.io.tmpdir"), filename);
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                resultWorkbook.write(fos);
            }
            resultWorkbook.close();

            BulkRegisterResponseDto dto = BulkRegisterResponseDto.builder()
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .reportDownloadUrl(filename)
                    .build();

            return ApiResponse.<BulkRegisterResponseDto>builder()
                    .statusCode(200)
                    .message("Bulk registration completed.")
                    .data(dto)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<BulkRegisterResponseDto>builder()
                    .statusCode(500)
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

        int success = (int) execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        int failure = (int) execution.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();

        return ApiResponse.<BulkRegisterResponseDto>builder()
                .statusCode(200)
                .message("Batch job accepted. Processing has started and may take a few moments.")
//                .data(BulkRegisterResponseDto.builder()
//                        .successCount(success)
//                        .failureCount(failure)
//                        .reportDownloadUrl("TBD") // implement if you still want to create report
//                        .build())
                .build();
    }

}
