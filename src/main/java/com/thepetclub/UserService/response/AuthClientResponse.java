package com.thepetclub.UserService.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AuthClientResponse {
    private String message;
    private boolean isVerified;
}
