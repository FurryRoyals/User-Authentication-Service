package theworldofpuppies.UserService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.TemporaryUser;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.TemporaryUserRepository;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.utils.GenerateOTP;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SimpleRegisterService {

    private final RegisterService registerService;
    private final TemporaryUserRepository temporaryUserRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final GenerateOTP generateOTP;

    @Transactional
    public boolean verifyOtp(String phoneNumber, String otp) {
        // Retrieve user and temporary user information
        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Something went wrong, please try again"));

        // Check if OTP is valid and not expired
        if (!isOtpValid(tempUser, otp)) {
            return false;
        }

        userRepository.save(createNewUser(tempUser));

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
                "",
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
    public void sendOtpForVerification(String phoneNumber, String email, String role) {

        TemporaryUser tempUser = temporaryUserRepository.findByPhoneNumber(phoneNumber)
                .orElse(null);

        String otp = generateOTP.generateOtp(); // Generate a new OTP
        String otpMessage = "Dear customer, your OTP is " + otp + " and is valid for 10 minutes.";
        System.out.println(otpMessage);

//        otpService.sendOtpToPhoneNumber(phoneNumber, otp); // Ensure otpService sends the OTP correctly
//
        if (tempUser == null) {
            // Create a new temporary user if one does not exist
            tempUser = new TemporaryUser(
                    "",
                    email,
                    phoneNumber,
                    false,
                    false,
                    "",
                    registerService.getRolesBasedOnRole(role), // Default roles (USER or ADMIN)
                    otp,
                    LocalDateTime.now().plusMinutes(10) // Set OTP expiration time
            );
        } else {
            tempUser.setOtp(otp);
            tempUser.setOtpExpirationTime(LocalDateTime.now().plusMinutes(10)); // Set OTP expiration time
            tempUser.setPhoneNumberVerified(false);
        }

        temporaryUserRepository.save(tempUser);
    }
}

