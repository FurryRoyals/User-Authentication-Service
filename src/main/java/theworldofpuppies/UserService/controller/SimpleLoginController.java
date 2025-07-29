package theworldofpuppies.UserService.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theworldofpuppies.UserService.dto.UserDto;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.RegisterService;
import theworldofpuppies.UserService.service.SimpleLoginService;
import theworldofpuppies.UserService.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("${prefix}/login")
@RequiredArgsConstructor
public class SimpleLoginController {
    private static final Logger log = LoggerFactory.getLogger(SimpleLoginController.class);
    private final JwtUtils jwtUtils;

    private final SimpleLoginService simpleLoginService;
    private final RegisterService registerService;

    @PostMapping("/{role}/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @PathVariable("role") String role,
            @RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        boolean isUserExist = registerService.checkPhoneNumberForVerification(phoneNumber);
        try {
            if (!isUserExist) {
                return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("User doesn't exist", false, null));
            }
            if (!phoneNumber.isEmpty()) {
                simpleLoginService.sendOtpForVerification(phoneNumber, role);
                return ResponseEntity.ok(new ApiResponse("Otp has been sent successfully", true, null));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(e.getMessage(), false, null));
        }
        return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("Something went wrong", false, null));
    }

    @PostMapping("user/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = simpleLoginService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            User savedUser = registerService.getUserByPhoneNumber(phoneNumber);
            String token = "Bearer " + jwtUtils.generateToken(phoneNumber);
            long expirationTime = jwtUtils.getExpirationTime().getTime();
            UserDto userResponse = new UserDto(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getPhoneNumber(),
                    token,
                    expirationTime);
            return ResponseEntity.ok(new ApiResponse("Phone number verified successfully.", true, userResponse));
        } else {
            return ResponseEntity.status(BAD_REQUEST).body(
                    new ApiResponse("Invalid OTP or OTP expired.", false, null));
        }
    }
}
