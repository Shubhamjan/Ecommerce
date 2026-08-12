package com.demo.service;

import com.demo.dto.ForgetPasswordReset;
import com.demo.dto.ResetPasswordRequest;
import com.demo.dto.VerifyOtpRequest;
import jakarta.validation.Valid;

public interface ForgetPasswordService {



    void sendOtp(@Valid ForgetPasswordReset request);

    void verifyOtp(@Valid VerifyOtpRequest request);

    void resetPassword(@Valid ResetPasswordRequest request);
}
