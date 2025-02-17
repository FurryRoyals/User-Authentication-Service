package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.TemporaryUser;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.TemporaryUserRepository;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.utils.GenerateOTP;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;

    private final GenerateOTP generateOTP;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final TemporaryUserRepository temporaryUserRepository;

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
        return (userRepository.findByPhoneNumber(phoneNumber))
                .map(User::isPhoneNumberVerified)
                .orElse(false);
    }


    // Method to save a new user (or admin) to the database
    public User saveNewUser(String phoneNumber, String password, String username, String role) {
        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Temporary user not found with: " + phoneNumber));

        User newUser = new User(
                username,
                tempUser.getEmail(),
                "",
                null,
                tempUser.getPhoneNumber(),
                tempUser.isPhoneNumberVerified(),
                passwordEncoder.encode(password),
                LocalDateTime.now(),
                getRolesBasedOnRole(role) // Assign roles (USER or ADMIN)
        );

        User savedUser = userRepository.save(newUser);
        temporaryUserRepository.delete(tempUser);
        return savedUser;
    }


    // Method to get roles based on the given role
    public List<String> getRolesBasedOnRole(String role) {
        List<String> roles = new ArrayList<>();
        if ("admin".equalsIgnoreCase(role)) {
            roles.add("USER");
            roles.add("ADMIN");
        } else {
            roles.add("USER");
        }
        return roles;
    }

    // Method to verify OTP for a user (or admin)
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

    // Method to send OTP for phone number verification
    public void sendOtpForVerification(String phoneNumber, String role) {
        // Check if the user already exists
        User existingUser = userRepository.findByPhoneNumber(phoneNumber).orElseGet(null);

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
                        "",
                        "",
                        phoneNumber,
                        false,
                        false,
                        "",
                        getRolesBasedOnRole(role), // Default roles (USER or ADMIN)
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

    // Method to retrieve a user by phone number
    public User getUserByPhoneNumber(String phoneNumber) {
        return (userRepository.findByPhoneNumber(phoneNumber))
                .orElseThrow(() -> new ResourceNotFoundException("No user found with phoneNumber: " + phoneNumber));
    }


    public TemporaryUser getTempUserByPhoneNumber(String phoneNumber) {
        Optional<TemporaryUser> tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber);
        return tempUser.orElse(null);
    }
}


