package com.thepetclub.UserService.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    UserDetails loadUserByPhoneNumber(String phoneNumber);
}
