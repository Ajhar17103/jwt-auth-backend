package com.example.jwt.mapper;

import com.example.jwt.dto.RegisterResponseDto;
import com.example.jwt.entity.Users;
import com.example.jwt.params.RegisterRequestParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    Users toUser(RegisterRequestParams dto);

    RegisterResponseDto toResponseDto(Users user);
}

