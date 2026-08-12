package com.demo.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequestDto {

    @NotBlank(message = "Name required")
    private String name;

    @Email(message = "Valid Email Required")
    @NotBlank(message = "Email required")
    private String email;

    @NotBlank(message = "password required")
    @Size(min=6, message = "Password must be at least 6 character")
    private String password;

    private String phone;
}
