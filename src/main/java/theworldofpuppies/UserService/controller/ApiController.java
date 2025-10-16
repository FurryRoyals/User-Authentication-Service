package theworldofpuppies.UserService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import theworldofpuppies.UserService.dto.UserIdAndNameDto;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.exception.UnauthorizedException;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.response.AuthClientResponse;
import theworldofpuppies.UserService.response.WalletResponse;
import theworldofpuppies.UserService.service.ApiService;
import theworldofpuppies.UserService.service.referral.ReferralService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.List;

import static org.ietf.jgss.GSSException.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${prefix}/")
@Slf4j
public class ApiController {

    private final ApiService apiService;
    private final JwtUtils jwtUtils;
    private final ReferralService referralService;

    @GetMapping("/validate-admin")
    public AuthClientResponse validateAdmin(@RequestParam String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            boolean isVerified = apiService.validateAdmin(phoneNumber);
            if (isVerified) {
                return new AuthClientResponse("Admin verified", true, null);
            }
        } catch (ResourceNotFoundException | UnauthorizedException e) {
            return new AuthClientResponse(e.getMessage(), false, null);
        }
        return new AuthClientResponse("Unexpected error", false, null);
    }

    @GetMapping("/validate-user/{token}")
    public AuthClientResponse validateUser(@PathVariable String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid token");
            }
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            boolean isVerified = apiService.validateUser(phoneNumber);
            if (isVerified) {
                String userId = apiService.getUserId(phoneNumber);
                return new AuthClientResponse("User verified", true, userId);
            }
        } catch (ResourceNotFoundException | UnauthorizedException e) {
            return new AuthClientResponse(e.getMessage(), false, null);
        }
        return new AuthClientResponse("Unexpected error", false, null);
    }

    @GetMapping("user/name/id")
    public ApiResponse getUserNames(
            @RequestParam List<String> userIds
    ) {
        try {
            List<UserIdAndNameDto> userIdAndNameDtos = apiService.getUserNames(userIds);
            return new ApiResponse("Got the user names", true, userIdAndNameDtos);
        } catch (RuntimeException e) {
            return new ApiResponse(e.getMessage(), false, null);
        }
    }

    @GetMapping("user/wallet/balance")
    public WalletResponse getWalletBalance(
            @RequestParam String userId
    ) {
        try {
            Double balance = referralService.getWalletBalance(userId);
            return new WalletResponse("fetched successfully", true, balance);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new WalletResponse(e.getMessage(), false, null);
        }
    }

}
