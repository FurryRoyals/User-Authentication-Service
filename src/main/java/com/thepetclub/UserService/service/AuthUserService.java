package com.thepetclub.UserService.service;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.UserRepository;
import com.thepetclub.UserService.utils.GenerateOTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GenerateOTP generateOTP;

    @Autowired
    private OtpService otpService;

    public void setEmail(String phoneNumber, String email) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.isPhoneNumberVerified()) {
            user.setEmail(email);
            userRepository.save(user);
        }
    }

    public void updatePassword(String phoneNumber, String newPassword) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null && user.isPhoneNumberVerified()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }


    public boolean verifyEmail(String phoneNumber, String otp) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber);
        if (existingUser.getEmailOtp().equals(otp) && LocalDateTime.now().isBefore(existingUser.getOtpExpirationTime())) {
            existingUser.setEmailOtp(null);
            existingUser.setOtpExpirationTime(null);
            existingUser.setEmailVerified(true);
            userRepository.save(existingUser);
        }
        return existingUser.isEmailVerified();
    }

    public void sendOtpForEmailVerification(String phoneNumber, String email) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber);
        if (existingUser != null && existingUser.isPhoneNumberVerified()) {
            String otp = generateOTP.generateOtp();
            otpService.sendOtpToEmail(email, otp);
            existingUser.setEmailOtp(otp);
            userRepository.save(existingUser);
        }
    }

    public boolean checkEmailAvailability(String email) {
        User existingUser = userRepository.findByEmail(email);
        return existingUser == null;
    }
}
