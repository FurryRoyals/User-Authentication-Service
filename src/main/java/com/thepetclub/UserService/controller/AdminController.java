package com.thepetclub.UserService.controller;

import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.service.AdminRegisterService;
import com.thepetclub.UserService.service.RegisterService;
import com.thepetclub.UserService.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("auth/ADMIN")
public class AdminController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AdminRegisterService adminregisterService;

    @PostMapping("/save")
    public ResponseEntity<?> saveNewAdmin(@RequestBody Map<String, String> admin) {
        try {
            // Extract values from the admin map
            String username = admin.get("username");
            String email = admin.get("email");
            String phoneNumber = admin.get("phoneNumber");
            boolean emailVerified = Boolean.parseBoolean(admin.get("emailVerified"));
            boolean phoneNumberVerified = Boolean.parseBoolean(admin.get("phoneNumberVerified"));
            String password = admin.get("password");

            // Call the service method to save the new admin
            boolean isNewAdmin = adminregisterService.saveNewAdmin(username, email, phoneNumber, emailVerified, phoneNumberVerified, password);
            if (isNewAdmin) {
                return new ResponseEntity<>("Admin saved successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("phoneNumber is alreay in use", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while saving the admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}



