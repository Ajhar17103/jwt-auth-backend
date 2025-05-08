package com.example.jwt.dto;

import com.example.jwt.entity.Users;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponseDto {
    private String name;
    private String email;
    private String role;
    private boolean isActive;
    private int isDeleted;
}