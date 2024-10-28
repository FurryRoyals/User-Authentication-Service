package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.service.AuthUserService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${prefix}/")
@Slf4j
@RequiredArgsConstructor
public class AuthUserController {

    private final JwtUtils jwtUtils;

    private final AuthUserService authUserService;

    @PutMapping("/user/set-email")
    public ResponseEntity<?> setEmail(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        String email = user.get("email");
        String otp = user.get("otp");
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
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
    public ResponseEntity<?> sendOtpForEmailVerification(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        String email = user.get("email");
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
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
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            // Proceed to update the password
            String newPassword = user.get("password");
            authUserService.updatePassword(phoneNumber, newPassword);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating password", e);
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

