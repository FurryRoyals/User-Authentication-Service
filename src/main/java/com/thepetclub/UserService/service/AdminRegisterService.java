package com.thepetclub.UserService.service;

import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRegisterService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public boolean saveNewAdmin(
            String username, String email,
            String phoneNumber, boolean emailVerified,
            boolean phoneNumberVerified, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            user = new User(
                    username,
                    email,
                    phoneNumber,
                    emailVerified,
                    phoneNumberVerified,
                    passwordEncoder.encode(password),
                    LocalDateTime.now(),
                    List.of("ADMIN", "USER")
            );
            userRepository.save(user);
            return true;
        } else {
            return false;
        }

    }

    public boolean checkPhoneNumberForVerification(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        return user != null && user.isPhoneNumberVerified();
    }

}
