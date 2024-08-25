package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.service.AuthUserService;
import com.thepetclub.UserService.service.RegisterService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
@Slf4j
public class AuthUserController {

    @Autowired
    private AuthUserService authUserService;

    @PutMapping("/user/set-email")
    public ResponseEntity<?> setEmail(@RequestBody Map<String, String> user) {
        String phoneNumber = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = user.get("email");
        String otp = user.get("otp");
        try {
            boolean isEmailVerified = authUserService.verifyEmail(phoneNumber, otp);
            if (isEmailVerified) {
                authUserService.setEmail(phoneNumber, email);
                return new ResponseEntity<>("Email added successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid OTP", HttpStatus.UNAUTHORIZED);
            }
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating email", e);
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/send-otp-to-email")
    public ResponseEntity<?> sendOtpForEmailVerification(@RequestBody Map<String, String> user) {
        String phoneNumber = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = user.get("email");
        try {
            boolean isEmailAvailable = authUserService.checkEmailAvailability(email);
            if (isEmailAvailable) {
                authUserService.sendOtpForEmailVerification(phoneNumber, email);
                return new ResponseEntity<>("OTP has been sent successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("A user with this email already exists", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error sending OTP to email", e);
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> user) {
        String phoneNumber = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String newPassword = user.get("password");
        try {
            authUserService.updatePassword(phoneNumber, newPassword);
            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating password", e);
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

