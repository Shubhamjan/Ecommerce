package com.demo.service.impl;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.LoginResponseDTO;
import com.demo.dto.UserCreateRequestDto;
import com.demo.dto.UserResponseDto;
import com.demo.entity.User;
import com.demo.exception.ResourceAlreadyExistException;
import com.demo.mapper.AuthMapper;
import com.demo.mapper.UserMapper;
import com.demo.repository.UserRepository;
import com.demo.service.UserService;
import com.demo.util.JwtUtil;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthMapper authMapper;

    @Test
    void shouldThrowException_whenEmailAlreadyExists(){

        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistException.class,()->{
            userService.register(dto);
        });

        verify(userRepository,never()).save(any());

    }

//    @Test
//    void shouldLoginSuccessfully(){
//
//        LoginRequestDto dto = new LoginRequestDto();
//
//        dto.setEmail("test@gmail.com");
//        dto.setPassword("123");
//
//        Authentication auth = mock(Authentication.class);
//
//        User user = new User();
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(auth);
//
//        when(auth.isAuthenticated()).thenReturn(true);
//
//        when(userRepository.findByEmail(dto.getEmail()))
//                .thenReturn(Optional.of(user));
//
//        when(jwtUtil.generateToken(any(), any(), any()))
//                .thenReturn("token");
//
//        when(authMapper.toLoginResponse(any(), any()))
//                .thenReturn(new LoginResponseDTO());
//
//        LoginResponseDTO response = userService.login(dto);
//
//        assertNotNull(response);
//
//        verify(jwtUtil).generateToken(any(),any(),any());
//    }

    @Test
    void shouldReturnUser_whenUserExists(){

        // Arrange
        Long id = 1L;
        User user = new User();
        UserResponseDto responseDto = new UserResponseDto();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        //Act
        UserResponseDto result = userService.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(userRepository).findById(id);
        verify(userMapper).toDto(user);

    }
}
