package com.thepetclub.UserService.service;

import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.TemporaryUserRepository;
import com.thepetclub.UserService.repository.UserRepository;
import com.thepetclub.UserService.utils.GenerateOTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordService {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private TemporaryUserRepository temporaryUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private GenerateOTP generateOTP;

    public void resetPassword(String phoneNumber, String newPassword) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber);
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existingUser);
    }

    public void sendOtpForVerification(String phoneNumber) {
        String otp = generateOTP.generateOtp();
        otpService.sendOtpToPhoneNumber(phoneNumber, otp);
        TemporaryUser temporaryUser = new TemporaryUser(
                phoneNumber,
                false,
                otp,
                LocalDateTime.now()
        );
        temporaryUserRepository.save(temporaryUser);

    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber);
        if (existingUser != null && existingUser.isPhoneNumberVerified()) {
            Optional<TemporaryUser> temporaryUser = temporaryUserRepository.findByPhoneNumber(phoneNumber);
            if (temporaryUser.isPresent()) {
                TemporaryUser tempUser = temporaryUser.get();
                if (tempUser.getOtp().equals(otp) && LocalDateTime.now().isBefore(tempUser.getOtpExpirationTime())) {
                    temporaryUserRepository.delete(tempUser);
                    return true; // OTP verified successfully
                }
            }
        }
        return false; // OTP verification failed
    }

}
