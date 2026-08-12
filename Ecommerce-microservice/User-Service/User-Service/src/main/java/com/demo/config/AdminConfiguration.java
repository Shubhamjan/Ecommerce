package com.demo.config;

import com.demo.entity.User;
import com.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


//This is the database seeding
@Configuration
public class AdminConfiguration {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return args -> {
            System.out.println("🔥 Admin runner executing...");
            Optional<User> fetchedUser = userRepository.findByEmail("admin@shop.com");
            if(!fetchedUser.isPresent()){
                User admin = new User();

                admin.setEmail("admin@shop.com");
                admin.setPassword(passwordEncoder.encode("admin@123"));
                admin.setRoles("ROLE_ADMIN");
                User saved = userRepository.save(admin);
                System.out.println("The user has saved :- "+saved.getEmail());
            }
        };
    }
}
