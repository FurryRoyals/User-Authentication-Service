package theworldofpuppies.UserService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.UpdateUserDto;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.request.UpdateUserRequest;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.AuthUserService;
import theworldofpuppies.UserService.service.user.UpdateUser;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${prefix}")
@Slf4j
@RequiredArgsConstructor
public class AuthUserController {

    private final JwtUtils jwtUtils;
    private final AuthUserService authUserService;
    private final UpdateUser updateUser;

    @PostMapping ("/user/update")
    public ResponseEntity<ApiResponse> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart("request") UpdateUserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("Invalid token", false, null));
            }
            UpdateUserDto userDto = updateUser.updateUser(image, request, phoneNumber);

            return ResponseEntity.ok(new ApiResponse("Updated successfully", true, userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Error updating email", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred", false, null));
        }
    }

    @PutMapping("/user/set-email")
    public ResponseEntity<ApiResponse> setEmail(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        String email = user.get("email");
        String otp = user.get("otp");
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            boolean isEmailVerified = authUserService.verifyEmail(phoneNumber, otp);
            if (isEmailVerified) {
                authUserService.setEmail(phoneNumber, email);
                return ResponseEntity.ok(new ApiResponse("Email added successfully", true, null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("Invalid OTP", false, null));
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Error updating email", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred", false, null));
        }
    }

    @PutMapping("/user/set-email/send-otp")
    public ResponseEntity<ApiResponse> sendOtpForEmailVerification(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        String email = user.get("email");
        try {

            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            boolean isEmailAvailable = authUserService.checkEmailAvailability(email);
            if (isEmailAvailable) {
                authUserService.sendOtpForEmailVerification(phoneNumber, email);
                return ResponseEntity.ok(new ApiResponse("OTP has been sent successfully", true, null));
            } else {
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new ApiResponse("A user with this email already exists", false, null));
            }
        } catch (Exception e) {
            log.error("Error sending OTP to email", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Something went wrong", false, null));
        }
    }

    @PutMapping("/user/update-password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            // Proceed to update the password
            String newPassword = user.get("password");
            authUserService.updatePassword(phoneNumber, newPassword);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating password", e);
            return new ResponseEntity<>("An error occurred", INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/update-username")
    public ResponseEntity<ApiResponse> updateUsername(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> user) {
        try {
            // Extract JWT token from the Authorization header
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            // Extract the phone number directly from the JWT token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);

            // (Optional) Additional manual validation of the token
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            // Proceed to update the password
            String username = user.get("username");
            authUserService.updateUsername(phoneNumber, username);

            return ResponseEntity.ok(new ApiResponse("Username updated successfully", true, null));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("Invalid token", false, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Error updating username", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("An error occurred", false, null));
        }
    }


}

