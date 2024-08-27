package com.thepetclub.UserService.model;

//import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "\"user\"")
@Document(collection = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private String emailOtp;
    private LocalDateTime otpExpirationTime;
    private String phoneNumber;
    private boolean phoneNumberVerified;
    private String password;
    private LocalDateTime createdAt;
    private List<String> roles;

    public User() {
    }

    public User(
            Long id,
            String username,
            String email,
            String phoneNumber,
            boolean emailVerified,
            boolean phoneNumberVerified,
            String password,
            LocalDateTime createdAt,
            List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.phoneNumberVerified = phoneNumberVerified;
        this.password = password;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public User(
            String username,
            String email,
            String emailOtp,
            LocalDateTime otpExpirationTime,
            String phoneNumber,
            boolean phoneNumberVerified,
            String password,
            LocalDateTime createdAt,
            List<String> roles) {
        this.username = username;
        this.email = email;
        this.emailOtp = emailOtp;
        this.otpExpirationTime = otpExpirationTime;
        this.phoneNumber = phoneNumber;
        this.phoneNumberVerified = phoneNumberVerified;
        this.password = password;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public User(
            String username,
            String email,
            String phoneNumber,
            boolean emailVerified,
            boolean phoneNumberVerified,
            String password,
            LocalDateTime createdAt,
            List<String> roles) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.phoneNumberVerified = phoneNumberVerified;
        this.password = password;
        this.createdAt = createdAt;
        this.roles = roles;
    }
}
