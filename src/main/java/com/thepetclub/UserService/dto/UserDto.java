package com.thepetclub.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class UserDto {
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String token;
    private Long expirationTime;
}
