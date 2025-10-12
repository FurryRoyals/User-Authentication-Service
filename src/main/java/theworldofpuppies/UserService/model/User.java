package theworldofpuppies.UserService.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private List<String> petIds = new ArrayList<>();
    private String username;
    private String email;
    private boolean emailVerified;
    private String emailOtp;
    private LocalDateTime otpExpirationTime;
    private String phoneNumber;
    private boolean phoneNumberVerified;
    private List<String> addressIds = new ArrayList<>();
    private String password;
    private LocalDateTime createdAt;
    private List<String> roles;
    private Image image;

    public User(
            String id,
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
