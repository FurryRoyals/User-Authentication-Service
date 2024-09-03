package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.service.RegisterService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
@Slf4j
@RequiredArgsConstructor
public class RegisterController {

    private final JwtUtils jwtUtils;

    private final RegisterService registerService;

    private final PasswordEncoder passwordEncoder;

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

    @PostMapping("/{role}/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = registerService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return new ResponseEntity<>("Phone number verified successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid OTP or OTP expired.", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{role}/send-otp")
    public ResponseEntity<?> sendOtp(
            @PathVariable("role") String role,
            @RequestBody Map<String, String> payload) {
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

    @PostMapping("/{role}/login")
    public ResponseEntity<String> login(@PathVariable("role") String role, @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String password = payload.get("password");

        if (phoneNumber.isBlank()) {
            return new ResponseEntity<>("PhoneNumber is required", HttpStatus.BAD_REQUEST);
        }

        boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
        log.debug("PhoneNumber: {} is verified: {}", phoneNumber, isVerified);

        if (!isVerified) {
            return new ResponseEntity<>("PhoneNumber not verified", HttpStatus.UNAUTHORIZED);
        }

        try {
            User user = registerService.getUserByPhoneNumber(phoneNumber);
            log.debug("User fetched: {}", user != null ? user.getPhoneNumber() : "null");

            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                log.debug("Password matches for user: {}", user.getPhoneNumber());

                if (user.getRoles().contains(role.toUpperCase())) {
                    String jwt = jwtUtils.generateToken(phoneNumber);
                    return new ResponseEntity<>(jwt, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Unauthorized role", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("Incorrect phone number or password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception occurred while creating authentication token", e);
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{role}/temp")
    public ResponseEntity<?> getUserByPhoneNumber(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        TemporaryUser tempUser = registerService.getTempUserByPhoneNumber(phoneNumber);
        System.out.println(tempUser);
        return new ResponseEntity<>(tempUser, HttpStatus.OK);
    }
}



