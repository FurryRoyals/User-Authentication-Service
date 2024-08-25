package com.thepetclub.UserService.service;

import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UsernameNotFoundException("No user found with phone number: " + phoneNumber);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhoneNumber())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Use loadUserByPhoneNumber instead");
    }
}
