package com.example.jwt.controller;


import com.example.jwt.dto.ReqRes;
import com.example.jwt.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersManagementController {
    @Autowired
    private UsersManagementService usersManagementService;

    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes req) {
        return ResponseEntity.ok(usersManagementService.register(req));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.login(req));
    }

    @GetMapping("/auth/refresh-token")
    public ResponseEntity<ReqRes> userList(@RequestBody ReqRes refreshToken){
        return ResponseEntity.ok(usersManagementService.refreshToken(refreshToken));
    }

}
