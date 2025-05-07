package com.example.jwt.controller;


import com.example.jwt.dto.Response;
import com.example.jwt.params.Request;
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
    public ResponseEntity<Response> register(@Valid @RequestBody Request RegisterRequest) {
        Response response = authService.register(RegisterRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody Request loginRequest){
        Response response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Response> refreshToken(@RequestBody Response refreshToken){
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(@RequestBody Response req) {
        return ResponseEntity.ok(authService.logout(req));
    }

}
