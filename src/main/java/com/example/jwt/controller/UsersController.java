package com.example.jwt.controller;

import com.example.jwt.dto.Response;
import com.example.jwt.entity.Users;
import com.example.jwt.service.UsersManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UsersController {
    private final UsersManagementService usersManagementService;

    public UsersController(UsersManagementService usersManagementService) {
        this.usersManagementService = usersManagementService;
    }

    @GetMapping("/user-lists")
    public ResponseEntity<Response> userList(){
        return ResponseEntity.ok(usersManagementService.getAllUser());
    }
}
