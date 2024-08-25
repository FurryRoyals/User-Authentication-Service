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
    private TemporaryUserRepository temporaryUserRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpService otpService;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/user/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> user) {
        if (user != null) {
            String phoneNumber = user.get("phoneNumber");
            System.out.println(phoneNumber);
            if (phoneNumber == null || phoneNumber.isBlank()) {
                return new ResponseEntity<>("PhoneNumber is missing", HttpStatus.BAD_REQUEST);
            } else {
                boolean isVerified = registerService.checkPhoneNumberVerificationForCreation(phoneNumber);
                if (isVerified) {
                    registerService.saveNewUser(phoneNumber);
                    return new ResponseEntity<>("User created successfully", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("PhoneNumber not verified", HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/user/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = registerService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return new ResponseEntity<>("Email verified successfully." + isVerified, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid OTP or OTP expired." + isVerified, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String username = payload.get("username");
        String password = payload.get("password");
        System.out.println(phoneNumber + username + password);
        if (!phoneNumber.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
            if (isVerified) {
                return new ResponseEntity<>("A user with this phoneNumber already exists", HttpStatus.OK);
            }
            registerService.sendOtpForVerification(phoneNumber, username, password);
            return new ResponseEntity<>("Otp has been sent successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/user/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> payload) {
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
                    String jwt = jwtUtils.generateToken(phoneNumber);
                    return new ResponseEntity<>(jwt, HttpStatus.OK);
                }
            } catch (Exception e) {
                log.error("Exception occurred while createAuthenticationToken ", e);
                return new ResponseEntity<>("Incorrect email or password", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("PhoneNumber not verified", HttpStatus.UNAUTHORIZED);
    }
}

