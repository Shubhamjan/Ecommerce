package com.demo.controller;

import com.demo.dto.*;
import com.demo.service.ForgetPasswordService;
import com.demo.service.UserService;
import com.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    private final ForgetPasswordService forgetPasswordService;

    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreateRequestDto dto){

        UserResponseDto created = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDto dto){
       LoginResponseDTO resp =  userService.login(dto);
       return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/get/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id, Authentication authentication){

        UserResponseDto dto = userService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){

        String authHeader = request.getHeader("Authorization");

        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.badRequest().body("No token found");
        }

        String token = authHeader.substring(7);

        long ttl = jwtUtil.getRemainingTime(token);

        if(ttl>0){
            userService.blackListToken(token,ttl);
        }

        return ResponseEntity.ok("Logged out succssfully");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody ForgetPasswordReset request){

        forgetPasswordService.sendOtp(request);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        forgetPasswordService.verifyOtp(request);
        return ResponseEntity.ok("OTP verified successfully");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        forgetPasswordService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-users")
    public ResponseEntity<List<UserResponseDto>> getAllUser(){
        List<UserResponseDto>users =  userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


}
