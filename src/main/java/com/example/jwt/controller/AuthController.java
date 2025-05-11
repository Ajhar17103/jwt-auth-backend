package com.example.jwt.controller;


import com.example.jwt.dto.*;
import com.example.jwt.params.LogoutRequestParams;
import com.example.jwt.params.RefreshTokenRequestParams;
import com.example.jwt.params.LoginRequestParams;
import com.example.jwt.params.RegisterRequestParams;
import com.example.jwt.service.AuthService;
import java.io.IOException;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/public/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> register(@Valid @RequestBody RegisterRequestParams registerRequest) {
        ApiResponse<RegisterResponseDto> registerRes = authService.register(registerRequest);
        return buildResponse(registerRes);
    }

    @PostMapping("/public/bulk-register")
    public ResponseEntity<ApiResponse<BulkRegisterResponseDto>> bulkRegister(@RequestParam("file") MultipartFile file) {
        ApiResponse<BulkRegisterResponseDto> reportInfo = authService.registerUsersInBatchAndSaveReport(file);
        return buildResponse(reportInfo);
    }

    @GetMapping("/public/bulk-register-report/{filename}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String filename) throws IOException {
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), filename);

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @PostMapping("/public/login")
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
