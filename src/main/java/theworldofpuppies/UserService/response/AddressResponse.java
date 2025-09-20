package theworldofpuppies.UserService.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import theworldofpuppies.UserService.model.Address;

@Data
@AllArgsConstructor
public class AddressResponse {
    private String message;
    private Boolean success;
    private Address address;
}
