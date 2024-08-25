package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.TemporaryUserRepository;
import com.thepetclub.UserService.service.OtpService;
import com.thepetclub.UserService.service.RegisterService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
@Slf4j
public class RegisterController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // User and Admin registration
    @PutMapping("/{role}/signup")
    public ResponseEntity<?> signup(@PathVariable("role") String role, @RequestBody Map<String, String> user) {
        if (user != null) {
            String phoneNumber = user.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.isBlank()) {
                return new ResponseEntity<>("PhoneNumber is missing", HttpStatus.BAD_REQUEST);
            } else {
                boolean isVerified = registerService.checkPhoneNumberVerificationForCreation(phoneNumber);
                if (isVerified) {
                    // Handle both user and admin roles
                    registerService.saveNewUser(phoneNumber, role);
                    return new ResponseEntity<>(role + " created successfully", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("PhoneNumber not verified", HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // OTP verification for both User and Admin
    @PostMapping("/{role}/verify-otp")
    public ResponseEntity<?> verifyOtp(@PathVariable("role") String role, @RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = registerService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return new ResponseEntity<>("Phone number verified successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid OTP or OTP expired.", HttpStatus.BAD_REQUEST);
        }
    }

    // Sending OTP for User and Admin
    @PostMapping("/{role}/send-otp")
    public ResponseEntity<?> sendOtp(@PathVariable("role") String role, @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String username = payload.get("username");
        String password = payload.get("password");
        if (!phoneNumber.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
            if (isVerified) {
                return new ResponseEntity<>("A user with this phoneNumber already exists", HttpStatus.OK);
            }
            registerService.sendOtpForVerification(phoneNumber, username, password, role);
            return new ResponseEntity<>("Otp has been sent successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
    }

    // User and Admin login
    @PostMapping("/{role}/login")
    public ResponseEntity<String> login(@PathVariable("role") String role, @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String password = payload.get("password");
        if (phoneNumber.isBlank()) {
            return new ResponseEntity<>("PhoneNumber is required", HttpStatus.BAD_REQUEST);
        }
        boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
        if (isVerified) {
            try {
                User user = registerService.getUserByPhoneNumber(phoneNumber);
                if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                    if (user.getRoles().contains(role.toUpperCase())) {
                        String jwt = jwtUtils.generateToken(phoneNumber);
                        return new ResponseEntity<>(jwt, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Unauthorized role", HttpStatus.UNAUTHORIZED);
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred while creating authentication token ", e);
                return new ResponseEntity<>("Incorrect phone number or password", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("PhoneNumber not verified", HttpStatus.UNAUTHORIZED);
    }
}



