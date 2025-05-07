package com.example.jwt.controller;


import com.example.jwt.dto.Response;
import com.example.jwt.params.Request;
import com.example.jwt.service.UsersManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsersManagementController extends BaseController {

    private final UsersManagementService usersManagementService;

    public UsersManagementController(UsersManagementService usersManagementService) {
        this.usersManagementService = usersManagementService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Response> register(@Valid @RequestBody Request RegisterRequest) {
        Response response =usersManagementService.register(RegisterRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Response> login(@RequestBody Request loginRequest){
        Response response = usersManagementService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<Response> refreshToken(@RequestBody Response refreshToken){
        return ResponseEntity.ok(usersManagementService.refreshToken(refreshToken));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Response> logout(@RequestBody Response req) {
        return ResponseEntity.ok(usersManagementService.logout(req));
    }

    @GetMapping("/user/user-list")
    public ResponseEntity<Response> userList(){
        return ResponseEntity.ok(usersManagementService.getAllUser());
    }

}
