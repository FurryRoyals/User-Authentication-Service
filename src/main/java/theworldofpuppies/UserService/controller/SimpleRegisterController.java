package theworldofpuppies.UserService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import theworldofpuppies.UserService.dto.UserDto;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.SimpleRegisterService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.lang.reflect.Executable;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${prefix}/register")
@RequiredArgsConstructor
@Slf4j
public class SimpleRegisterController {
    private final JwtUtils jwtUtils;

    private final SimpleRegisterService simpleRegisterService;
    private final ModelMapper modelMapper;

    @PostMapping("/{role}/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @PathVariable("role") String role,
            @RequestBody Map<String, String> payload) {
        try {
            String phoneNumber = payload.get("phoneNumber");
            String email = payload.get("email");
            String username = payload.get("username");
            boolean isUserExist = simpleRegisterService.checkPhoneNumberForVerification(phoneNumber);
            if (isUserExist) {
                return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("User already exists!", false, null));
            }
            if (!phoneNumber.isEmpty() && !email.isEmpty() && !username.isEmpty()) {
                simpleRegisterService.sendOtpForVerification(phoneNumber, email, username, role);
                return ResponseEntity.ok(new ApiResponse("Otp has been sent successfully", true, null));
            }
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("Something went wrong", false, null));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),false, null));
        }
    }

    @PostMapping("user/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody Map<String, String> user) {
        String phoneNumber = user.get("phoneNumber");
        String otp = user.get("otp");
        boolean isVerified = simpleRegisterService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            User savedUser = simpleRegisterService.getUserByPhoneNumber(phoneNumber);
            String token = "Bearer " + jwtUtils.generateToken(phoneNumber);
            long expirationTime = jwtUtils.getExpirationTime().getTime();
            UserDto userResponse = convertToDto(savedUser);
            userResponse.setToken(token);
            userResponse.setExpirationTime(expirationTime);
            return ResponseEntity.ok(new ApiResponse("Phone number verified successfully.", true, userResponse));
        } else {
            return ResponseEntity.status(BAD_REQUEST).body(
                    new ApiResponse("Invalid OTP or OTP expired.", false, null));
        }
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
