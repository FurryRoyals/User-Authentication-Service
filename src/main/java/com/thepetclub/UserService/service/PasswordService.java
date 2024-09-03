package com.thepetclub.UserService.service;

import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.TemporaryUserRepository;
import com.thepetclub.UserService.repository.UserRepository;
import com.thepetclub.UserService.utils.GenerateOTP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordService {

    private final RegisterService registerService;

    private final TemporaryUserRepository temporaryUserRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final GenerateOTP generateOTP;

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
                LocalDateTime.now().plusMinutes(10)
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
