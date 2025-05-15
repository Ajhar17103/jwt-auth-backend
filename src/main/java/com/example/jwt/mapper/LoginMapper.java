package com.example.jwt.mapper;

import com.example.jwt.dto.LoginResponseDto;
import com.example.jwt.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMapper {
    LoginResponseDto toResponseDto(Users user);
}

