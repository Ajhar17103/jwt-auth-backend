package com.example.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private int id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private int is_deleted;

}
