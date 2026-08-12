package com.demo.service;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.LoginResponseDTO;
import com.demo.dto.UserCreateRequestDto;
import com.demo.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto register(UserCreateRequestDto dto);

    LoginResponseDTO login(LoginRequestDto dto);

    UserResponseDto getById(Long id);

    String logout();

    void blackListToken(String token, long ttl);

    List<UserResponseDto> getAllUsers();
}
