package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.dto.UserDto;
import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.response.ApiResponse;
import com.thepetclub.UserService.service.RegisterService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${prefix}/")
@Slf4j
@RequiredArgsConstructor
public class RegisterController {

    private final JwtUtils jwtUtils;

    private final RegisterService registerService;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/{role}/signup")
    public ResponseEntity<ApiResponse> signup(@PathVariable("role") String role, @RequestBody Map<String, String> user) {
        if (user != null) {
            String phoneNumber = user.get("phoneNumber");
            String username = user.get("username");
            String password = user.get("password");
            if (phoneNumber == null || phoneNumber.isBlank() || password.isBlank() || password.isEmpty()) {
                return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("PhoneNumber are password are required", false, null));
            } else {
                boolean isVerified = registerService.checkPhoneNumberVerificationForCreation(phoneNumber);
                if (isVerified) {
                    // Handle both user and admin roles
                    User savedNewUser = registerService.saveNewUser(phoneNumber, password, username, role);
                    String token = "Bearer " + jwtUtils.generateToken(phoneNumber);
                    long expirationTime = jwtUtils.getExpirationTime().getTime();
                    UserDto userResponse = new UserDto(
                            savedNewUser.getId(),
                            savedNewUser.getUsername(),
                            savedNewUser.getEmail(),
                            savedNewUser.getPhoneNumber(),
                            token,
                            expirationTime);
                    return ResponseEntity.status(CREATED).body(
                            new ApiResponse("registration has been done successfully", true, userResponse));
                } else {
                    return ResponseEntity.status(BAD_REQUEST).body(
                            new ApiResponse("PhoneNumber not verified", false, null));
                }
            }
        }
        return new ResponseEntity<>(BAD_REQUEST);
    }

    @PostMapping("/{role}/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = registerService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("Phone number verified successfully.", true, null));
        } else {
            return ResponseEntity.status(BAD_REQUEST).body(
                    new ApiResponse("Invalid OTP or OTP expired.", false, null));
        }
    }

    @PostMapping("/{role}/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @PathVariable("role") String role,
            @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        if (!phoneNumber.isEmpty()) {
            boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
            if (isVerified) {
                return ResponseEntity.ok(
                        new ApiResponse("An user with this number already exists", false, null));
            }
            registerService.sendOtpForVerification(phoneNumber, role);
            return ResponseEntity.ok(new ApiResponse("Otp has been sent successfully", true, null));
        }
        return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("Something went wrong", false, null));
    }

    @PostMapping("/{role}/login")
    public ResponseEntity<ApiResponse> login(@PathVariable("role") String role, @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String password = payload.get("password");

        if (phoneNumber.isBlank()) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("PhoneNumber is required", false, null));
        }

        boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
        log.debug("PhoneNumber: {} is verified: {}", phoneNumber, isVerified);

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("PhoneNumber not verified", false, null));
        }

        try {
            User user = registerService.getUserByPhoneNumber(phoneNumber);
            log.debug("User fetched: {}", user != null ? user.getPhoneNumber() : "null");

            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                log.debug("Password matches for user: {}", user.getPhoneNumber());

                if (user.getRoles().contains(role.toUpperCase())) {
                    String token = "Bearer " + jwtUtils.generateToken(phoneNumber);
                    long expirationTime = jwtUtils.getExpirationTime().getTime();
                    UserDto userResponse = new UserDto(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            token,
                            expirationTime);
                    return ResponseEntity.ok(new ApiResponse("Login successful", true, userResponse));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Unauthorized role", false, null));
                }
            } else {
                return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("Incorrect phone number or password", false, null));
            }
        } catch (Exception e) {
            log.error("Exception occurred while creating authentication token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("An error occurred", false, null));
        }
    }


    @PostMapping("/{role}/temp")
    public ResponseEntity<?> getUserByPhoneNumber(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        TemporaryUser tempUser = registerService.getTempUserByPhoneNumber(phoneNumber);
        System.out.println(tempUser);
        return new ResponseEntity<>(tempUser, OK);
    }
}



