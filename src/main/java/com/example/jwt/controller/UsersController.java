package com.example.jwt.controller;

import com.example.jwt.dto.Response;
import com.example.jwt.params.Request;
import com.example.jwt.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/user-lists")
    public ResponseEntity<Response> userList(){
        return ResponseEntity.ok(usersService.getAllUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> userById(@PathVariable int id) {
        Response userDetails = usersService.userDetails(id);
        return ResponseEntity.ok(userDetails);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateUser(@Valid @PathVariable int id, @RequestBody Request users) {
       Response user = usersService.updateUser(id, users);
       return ResponseEntity.ok(user);
    }

}
