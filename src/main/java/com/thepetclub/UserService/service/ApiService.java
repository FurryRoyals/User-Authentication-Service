package com.thepetclub.UserService.service;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.exception.UnauthorizedException;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApiService {

    @Autowired
    private UserRepository userRepository;

    public boolean validateAdmin(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!user.isPhoneNumberVerified()) {
            throw new UnauthorizedException("Phone number is not verified");
        }
        if (!user.getRoles().contains("ADMIN")) {
            throw new UnauthorizedException("Unauthorized access!");
        }
        return true;
    }
}
