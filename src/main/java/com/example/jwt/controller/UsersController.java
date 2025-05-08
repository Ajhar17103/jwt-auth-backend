package com.example.jwt.controller;

import com.example.jwt.dto.ApiResponse;
import com.example.jwt.dto.UserResponseDto;
import com.example.jwt.params.UserUpdateRequestParams;
import com.example.jwt.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UsersController extends BaseController{
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/user-lists")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> userList() {
        ApiResponse<List<UserResponseDto>> userDetails = usersService.getAllUser();
        return buildResponse(userDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> userById(@PathVariable int id) {
        ApiResponse<UserResponseDto> userDetails = usersService.userDetails(id);
        return buildResponse(userDetails);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@Valid @PathVariable int id, @RequestBody UserUpdateRequestParams users) {
        ApiResponse<UserResponseDto> user = usersService.updateUser(id, users);
       return buildResponse(user);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(@PathVariable int id) {
        ApiResponse<UserResponseDto> response = usersService.deleteUser(id);
        return buildResponse(response);
    }

}
