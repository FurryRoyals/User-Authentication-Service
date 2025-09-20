package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.TemporaryUser;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.TemporaryUserRepository;
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
public class PasswordService {

    private final TemporaryUserRepository temporaryUserRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final GenerateOTP generateOTP;

    public void resetPassword(String phoneNumber, String newPassword) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone Number not found with: " + phoneNumber));
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
        User existingUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone Number not found with: " + phoneNumber));

        if (existingUser.isPhoneNumberVerified()) {
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
