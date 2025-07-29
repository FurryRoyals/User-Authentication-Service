package theworldofpuppies.UserService.controller;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import theworldofpuppies.UserService.model.Address;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.AddressService;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("{prefix}")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PutMapping("set")
    public ResponseEntity<ApiResponse> setNewField() {
        try {
            UpdateResult products = addressService.setProductField();
            if (products.getModifiedCount() == 0) {
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to set new Field", false, null));
            }
            return ResponseEntity.ok(new ApiResponse("successfully set", true, products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to set new Field", false, null));
        }
    }

    @PostMapping("address/set")
    public ResponseEntity<ApiResponse> setAddress(@RequestParam String userId, @RequestBody Address address) {
        try {
            addressService.setAddress(userId, address);
            return ResponseEntity.ok(new ApiResponse("address added successfully", true, address));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("address/get")
    public ResponseEntity<ApiResponse> getAddress(@RequestParam String userId) {
        try {
            List<Address> addresses = addressService.getAddresses(userId);
            return ResponseEntity.ok(new ApiResponse("address added successfully", true, addresses));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }


}
