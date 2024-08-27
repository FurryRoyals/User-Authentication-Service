package com.thepetclub.UserService.model;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
public class TemporaryUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private String phoneNumber;
    private boolean phoneNumberVerified;
    private String password;
    private List<String> roles;
    private String otp;
    private LocalDateTime otpExpirationTime;

    public TemporaryUser() {}

    public TemporaryUser(
            String username,
            String email,
            String phoneNumber,
            boolean emailVerified,
            boolean phoneNumberVerified,
            String password,
            List<String> roles,
            String otp,
            LocalDateTime otpExpirationTime) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.phoneNumberVerified = phoneNumberVerified;
        this.password = password;
        this.roles = roles;
        this.otp = otp;
        this.otpExpirationTime = otpExpirationTime;
    }

    public TemporaryUser(
            String phoneNumber,
            boolean phoneNumberVerified,
            String otp,
            LocalDateTime otpExpirationTime
    ) {
        this.phoneNumber = phoneNumber;
        this.phoneNumberVerified = phoneNumberVerified;
        this.otp = otp;
        this.otpExpirationTime = otpExpirationTime;
    }
}
