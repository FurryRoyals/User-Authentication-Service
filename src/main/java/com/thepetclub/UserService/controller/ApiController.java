package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.exception.UnauthorizedException;
import com.thepetclub.UserService.response.AuthClientResponse;
import com.thepetclub.UserService.service.ApiService;
import com.thepetclub.UserService.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("auth")
@Slf4j
public class ApiController {

    private final ApiService apiService;
    private final JwtUtils jwtUtils;

    @GetMapping("/validate-admin")
    public AuthClientResponse validateAdmin(@RequestParam String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            boolean isVerified = apiService.validateAdmin(phoneNumber);
            if (isVerified) {
                return new AuthClientResponse("Admin verified", true, null);
            }
        } catch (ResourceNotFoundException | UnauthorizedException e) {
            return new AuthClientResponse(e.getMessage(), false, null);
        }
        return new AuthClientResponse("Unexpected error", false, null);
    }

    @GetMapping("/validate-user/{token}")
    public AuthClientResponse validateUser(@PathVariable String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            boolean isVerified = apiService.validateUser(phoneNumber);
            if (isVerified) {
                String userId = apiService.getUserId(phoneNumber);
                return new AuthClientResponse("User verified", true, userId );
            }
        } catch (ResourceNotFoundException | UnauthorizedException e) {
            return new AuthClientResponse(e.getMessage(), false, null);
        }
        return new AuthClientResponse("Unexpected error", false, null);
    }

}
