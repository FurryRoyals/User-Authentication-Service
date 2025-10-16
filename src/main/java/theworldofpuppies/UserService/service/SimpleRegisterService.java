package theworldofpuppies.UserService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.TemporaryUser;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.ReferralRepository;
import theworldofpuppies.UserService.repository.TemporaryUserRepository;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.request.SignupRequest;
import theworldofpuppies.UserService.request.SignupVerificationRequest;
import theworldofpuppies.UserService.service.referral.ReferralService;
import theworldofpuppies.UserService.utils.GenerateOTP;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleRegisterService {

    private final TemporaryUserRepository temporaryUserRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final GenerateOTP generateOTP;
    private final ReferralService referralService;

    @Transactional
    public boolean verifyOtp(SignupVerificationRequest request) {
        // Retrieve user and temporary user information
        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Something went wrong, please try again"));

        // Check if OTP is valid and not expired
        if (!isOtpValid(tempUser, request.getOtp())) {
            return false;
        }

        User user = userRepository.save(createNewUser(tempUser));
        if (request.getReferralCode() != null && request.getReferralCode().isEmpty()) {
            referralService.createReferral(request.getReferralCode(), user.getId());
        }

        referralService.generateReferralCode(user.getId());


        // Clean up the temporary user record
        temporaryUserRepository.delete(tempUser);
        return true;
    }

    // Helper method to validate OTP
    private boolean isOtpValid(TemporaryUser tempUser, String otp) {
        return tempUser.getOtp().equals(otp) && LocalDateTime.now().isBefore(tempUser.getOtpExpirationTime());
    }

    // Helper method to create a new user from temporary user data
    private User createNewUser(TemporaryUser tempUser) {
        return new User(
                tempUser.getUsername(),
                tempUser.getEmail(),
                tempUser.getPhoneNumber(),
                false,
                true,
                "",
                LocalDateTime.now(),
                tempUser.getRoles()
        );
    }


    @Transactional
    public void sendOtpForVerification(SignupRequest request, String role) {

        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElse(null);

        String otp = generateOTP.generateOtp(); // Generate a new OTP
        String otpMessage = "Dear customer, your OTP is " + otp + " and is valid for 10 minutes.";
        System.out.println(otpMessage);

//        otpService.sendOtpToPhoneNumber(phoneNumber, otp); // Ensure otpService sends the OTP correctly
//
        if (tempUser == null) {
            // Create a new temporary user if one does not exist
            tempUser = new TemporaryUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    false,
                    false,
                    "",
                    getRolesBasedOnRole(role), // Default roles (USER or ADMIN)
                    otp,
                    LocalDateTime.now().plusMinutes(10) // Set OTP expiration time
            );
        } else {
            tempUser.setOtp(otp);
            tempUser.setOtpExpirationTime(LocalDateTime.now().plusMinutes(10)); // Set OTP expiration time
            tempUser.setPhoneNumberVerified(false);
            tempUser.setEmail(request.getEmail());
            tempUser.setUsername(request.getUsername());
        }

        temporaryUserRepository.save(tempUser);
    }

    public boolean checkPhoneNumberForVerification(String phoneNumber) {
        return (userRepository.findByPhoneNumber(phoneNumber))
                .map(User::isPhoneNumberVerified)
                .orElse(false);
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return (userRepository.findByPhoneNumber(phoneNumber))
                .orElseThrow(() -> new ResourceNotFoundException("No user found with phoneNumber: " + phoneNumber));
    }

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
}

