package com.thepetclub.UserService.service;

import com.thepetclub.UserService.exception.ResourceNotFoundException;
import com.thepetclub.UserService.model.TemporaryUser;
import com.thepetclub.UserService.model.User;
import com.thepetclub.UserService.repository.TemporaryUserRepository;
import com.thepetclub.UserService.repository.UserRepository;
import com.thepetclub.UserService.utils.GenerateOTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.desktop.PreferencesHandler;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
public class RegisterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenerateOTP generateOTP;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private TemporaryUserRepository temporaryUserRepository;

    public boolean checkPhoneNumberVerificationForCreation(String phoneNumber) {
        Optional<TemporaryUser> tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber);
        if (tempUser.isPresent()) {
            TemporaryUser temporaryUser = tempUser.get();
            return temporaryUser.isPhoneNumberVerified();
        } else {
            return false;
        }
    }


    public boolean checkPhoneNumberForVerification(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        return user != null && user.isPhoneNumberVerified();
    }

    public void saveNewUser(String phoneNumber) {
        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Temporary user not found"));
        User newUser = new User(
                tempUser.getUsername(),
                tempUser.getEmail(),
                "",
                null,
                tempUser.getPhoneNumber(),
                tempUser.isPhoneNumberVerified(),
                tempUser.getPassword(),
                LocalDateTime.now(),
                tempUser.getRoles()
        );
        userRepository.save(newUser);
        temporaryUserRepository.delete(tempUser);

    }


    public boolean verifyOtp(String phoneNumber, String otp) {
        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Something went wrong, please try again"));
        if (tempUser != null) {
            if (tempUser.getOtp().equals(otp) && LocalDateTime.now().isBefore(tempUser.getOtpExpirationTime())) {
                tempUser.setOtp(null);
                tempUser.setOtpExpirationTime(null);
                tempUser.setPhoneNumberVerified(true);
                temporaryUserRepository.save(tempUser);
                return true;
            }
        }
        return false;
    }

    public void sendOtpForVerification(String phoneNumber, String username, String password) {
        // Check if the user already exists
        User existingUser = userRepository.findByPhoneNumber(phoneNumber);

        if (existingUser == null) {
            // If the user does not exist, handle temporary user
            TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                    .orElse(null);

            String otp = generateOTP.generateOtp(); // Generate a new OTP
            String otpMessage = "Dear customer, your OTP is " + otp + " and is valid for 10 minutes.";
            System.out.println(otpMessage); // Print OTP message for debugging

            // Send OTP via SMS
            otpService.sendOtpToPhoneNumber(phoneNumber, otp); // Ensure otpService sends the OTP correctly

            if (tempUser == null) {
                // Create a new temporary user if one does not exist
                tempUser = new TemporaryUser(
                        username,
                        "",
                        phoneNumber,
                        false,
                        false,
                        passwordEncoder.encode(password),
                        Collections.singletonList("USER"),
                        otp,
                        LocalDateTime.now().plusMinutes(10) // Set OTP expiration time
                );
            } else {
                // Update existing temporary user
                tempUser.setOtp(otp);
                tempUser.setOtpExpirationTime(LocalDateTime.now().plusMinutes(10)); // Set OTP expiration time
                tempUser.setPhoneNumberVerified(false);
            }

            // Save or update the temporary user
            temporaryUserRepository.save(tempUser);
        } else {
            // User already exists, handle accordingly (if needed)
            System.out.println("User already exists with phone number: " + phoneNumber);
        }
    }


    public User getUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new ResourceNotFoundException("No user found with phoneNumber: " + phoneNumber);
        } else {
            return user;
        }
    }
}
