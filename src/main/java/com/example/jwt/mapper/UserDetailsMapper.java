package com.example.jwt.mapper;

import com.example.jwt.dto.UserResponseDto;
import com.example.jwt.entity.Users;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDetailsMapper {
    UserResponseDto toResponseDto(Users user);
    List<UserResponseDto> toResponseListDto(List<Users> users);
}
