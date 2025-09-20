package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.utils.GenerateOTP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final GenerateOTP generateOTP;

    private final OtpService otpService;
    
    private final String phoneNumberNotFound = "User not found with phone number: ";
    private final String phoneNumberNotVerified = "Phone number not verified";

    public void setEmail(String phoneNumber, String email) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        userOptional.orElseThrow(() -> new ResourceNotFoundException(phoneNumberNotFound + phoneNumber));
        User user = userOptional.get();
        if (user.isPhoneNumberVerified()) {
            user.setEmail(email);
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException(phoneNumberNotVerified);
        }
    }

    public void updateUsername(String phoneNumber, String username) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        userOptional.orElseThrow(() -> new ResourceNotFoundException(phoneNumberNotFound + phoneNumber));
        User user = userOptional.get();
        if (user.isPhoneNumberVerified()) {
            user.setUsername(username);
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException(phoneNumberNotVerified);
        }
    }

    public void updatePassword(String phoneNumber, String newPassword) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        userOptional.orElseThrow(() -> new ResourceNotFoundException(phoneNumberNotFound + phoneNumber));
        User user = userOptional.get();
        if (user.isPhoneNumberVerified()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException(phoneNumberNotVerified);
        }
    }

    public boolean verifyEmail(String phoneNumber, String otp) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        userOptional.orElseThrow(() -> new ResourceNotFoundException(phoneNumberNotFound + phoneNumber));
        User existingUser = userOptional.get();
        if (existingUser.getEmailOtp() != null && existingUser.getEmailOtp().equals(otp) && LocalDateTime.now().isBefore(existingUser.getOtpExpirationTime())) {
            existingUser.setEmailOtp(null);
            existingUser.setOtpExpirationTime(null);
            existingUser.setEmailVerified(true);
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    public void sendOtpForEmailVerification(String phoneNumber, String email) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        userOptional.orElseThrow(() -> new ResourceNotFoundException(phoneNumberNotFound + phoneNumber));
        User existingUser = userOptional.get();
        if (existingUser.isPhoneNumberVerified()) {
            String otp = generateOTP.generateOtp();
            otpService.sendOtpToEmail(email, otp);
            existingUser.setEmailOtp(otp);
            existingUser.setOtpExpirationTime(LocalDateTime.now().plusMinutes(10));
            userRepository.save(existingUser);
        } else {
            throw new ResourceNotFoundException(phoneNumberNotVerified);
        }
    }

    public boolean checkEmailAvailability(String email) {
        User existingUser = userRepository.findByEmail(email);
        return existingUser == null;
    }
}

