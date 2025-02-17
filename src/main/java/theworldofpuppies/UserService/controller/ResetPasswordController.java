package theworldofpuppies.UserService.controller;

import theworldofpuppies.UserService.service.PasswordService;
import theworldofpuppies.UserService.service.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${prefix}/")
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordController {

    private final PasswordService passwordService;

    private final RegisterService registerService;

    @PutMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String newPassword = payload.get("password");
        try {
            if (newPassword != null) {
                passwordService.resetPassword(phoneNumber, newPassword);
                return new ResponseEntity<>("Password has been successfully reset", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Password can't be empty", HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("error", e);
            return new ResponseEntity<>("Password reset failed", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PutMapping("/password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = passwordService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return new ResponseEntity<>("Email verified successfully." + isVerified, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid OTP or OTP expired." + isVerified, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        if (!phoneNumber.isEmpty()) {
            boolean isVerified = registerService.checkPhoneNumberForVerification(phoneNumber);
            if (isVerified) {
                passwordService.sendOtpForVerification(phoneNumber);
                return new ResponseEntity<>("Otp has been sent successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No user found with this phoneNumber", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
