package com.example.jwt.entity;

import com.example.jwt.controller.BaseController;
import com.example.jwt.dto.*;
import com.example.jwt.params.LoginRequestParams;
import com.example.jwt.params.LogoutRequestParams;
import com.example.jwt.params.RefreshTokenRequestParams;
import com.example.jwt.params.RegisterRequestParams;
import com.example.jwt.service.AuthService;
import io.jsonwebtoken.io.IOException;
import jakarta.annotation.Resource;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Entity
@Data
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String token;

    private Date expiration;

}
