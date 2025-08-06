package theworldofpuppies.UserService.controller;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import theworldofpuppies.UserService.exception.AlreadyExistsException;
import theworldofpuppies.UserService.exception.UnauthorizedException;
import theworldofpuppies.UserService.model.Address;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.AddressService;
import theworldofpuppies.UserService.service.ApiService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("{prefix}")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final JwtUtils jwtUtils;
    private final ApiService apiService;

    @PutMapping("set")
    public ResponseEntity<ApiResponse> setNewField() {
        try {
            UpdateResult products = addressService.setProductField();
            if (products.getModifiedCount() == 0) {
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to set new Field", false, null));
            }
            return ResponseEntity
                    .ok(new ApiResponse("successfully set", true, products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to set new Field", false, null));
        }
    }

    @PostMapping("address/add")
    public ResponseEntity<ApiResponse> setAddress(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Address address) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            Address savedAddress = addressService.addAddress(userId, address);
            return ResponseEntity
                    .ok(new ApiResponse("address added successfully", true, savedAddress));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @PutMapping("address/update")
    public ResponseEntity<ApiResponse> updateAddress(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Address address) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            Address savedAddress = addressService.updateAddress(userId, address);
            return ResponseEntity
                    .ok(new ApiResponse("address added successfully", true, savedAddress));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("address/get")
    public ResponseEntity<ApiResponse> getAddress(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            List<Address> addresses = addressService.getAddresses(userId);
            return ResponseEntity
                    .ok(new ApiResponse("address added successfully", true, addresses));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @PutMapping("address/update-selection")
    public ResponseEntity<ApiResponse> updateAddressSelection(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String addressId
    ) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            List<Address> updatedAddresses = addressService.updateAddressSelection(userId, addressId);
            return ResponseEntity
                    .ok(new ApiResponse("success", true, updatedAddresses));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @DeleteMapping("address/delete")
    public ResponseEntity<ApiResponse> deleteAddress(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String addressId
    ) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            List<Address> updatedAddresses = addressService.deleteAddress(userId, addressId);
            return ResponseEntity
                    .ok(new ApiResponse("success", true, updatedAddresses));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
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

