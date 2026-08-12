package com.demo.service.impl;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.LoginResponseDTO;
import com.demo.dto.UserCreateRequestDto;
import com.demo.dto.UserResponseDto;
import com.demo.entity.User;
import com.demo.exception.ResourceAlreadyExistException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.AuthMapper;
import com.demo.mapper.UserMapper;
import com.demo.repository.UserRepository;
import com.demo.service.UserService;
import com.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ListResourceBundle;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {


    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthMapper authMapper;

    private final JwtUtil jwtUtil;

    private final RedisTemplate<String,String> redisTemplate;

    private final AuthenticationManager authenticationManager;

    @Override
    public UserResponseDto register(UserCreateRequestDto dto) {

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new ResourceAlreadyExistException("Email already registered");
        }

        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDto dto) {

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getEmail(),dto.getPassword()
        ));
        if(auth.isAuthenticated()){
            User user =  userRepository.findByEmail(dto.getEmail()).orElseThrow(()->new ResourceNotFoundException("Invalid credentails"));

            String token = jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRoles());

            return authMapper.toLoginResponse(user,token);
        }else{
            throw new RuntimeException("Invalid credentials");
        }

    }

    @Override
    public UserResponseDto getById(Long id) {

        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Not found"));
        return userMapper.toDto(user);
    }

    @Override
    public String logout() {
        return "";
    }

    @Override
    public void blackListToken(String token, long ttl) {

        redisTemplate.opsForValue()
                .set(token,"BLACKLISTED",ttl, TimeUnit.MILLISECONDS);

    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto>response = users.stream().map(e->userMapper.toDto(e)).toList();
        return response;
    }
}
