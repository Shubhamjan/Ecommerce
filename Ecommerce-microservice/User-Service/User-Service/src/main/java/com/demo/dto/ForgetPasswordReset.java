package com.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordReset {

    @NotBlank
    @Email
    private String email;
}
