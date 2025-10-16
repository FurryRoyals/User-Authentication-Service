package theworldofpuppies.UserService.controller.referral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.exception.UnauthorizedException;
import theworldofpuppies.UserService.model.Referral;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.ApiService;
import theworldofpuppies.UserService.service.referral.ReferralService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.List;

import static org.ietf.jgss.GSSException.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${prefix}/referrals")
@Slf4j
public class ReferralController {

    private final ReferralService referralService;
    private final JwtUtils jwtUtils;
    private final ApiService apiService;

    @GetMapping
    public ResponseEntity<ApiResponse> getReferrals(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String referrerId = extractAndValidateTokenAndGetUser(authorizationHeader);
            List<Referral> referrals = referralService.getReferral(referrerId);
            if (referrals.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse("empty list", true, null));
            }
            return ResponseEntity.ok(new ApiResponse("fetched successfully", true, referrals));
        } catch (BadCredentialsException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("generate")
    public ResponseEntity<ApiResponse> generateReferralCode(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            String code = referralService.generateReferralCode(userId);
            return ResponseEntity.ok(new ApiResponse("generated successfully", true, code));
        } catch (BadCredentialsException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        }catch (ResourceNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("wallet/balance")
    public ResponseEntity<ApiResponse> getWalletBalance(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            Double balance = referralService.getWalletBalance(userId);
            return ResponseEntity.ok(new ApiResponse("fetched successfully", true, balance));
        } catch (BadCredentialsException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        }catch (ResourceNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }


    private String extractAndValidateTokenAndGetUser(String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        if (!jwtUtils.validateToken(token)) {
            throw new BadCredentialsException("Invalid token");
        }
        String phoneNumber = jwtUtils.extractPhoneNumber(token);
        boolean isVerified = apiService.validateUser(phoneNumber);
        if (!isVerified) {
            throw new UnauthorizedException("User is not authorized to access this data");
        }
        return apiService.getUserId(phoneNumber);
    }
}
