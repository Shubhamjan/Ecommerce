package com.demo.mapper;

import com.demo.dto.LoginResponseDTO;
import com.demo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {


    public LoginResponseDTO toLoginResponse(User user,String token){

        return new LoginResponseDTO(token,"Bearer",user.getId(), user.getEmail(), user.getName());
    }
}
