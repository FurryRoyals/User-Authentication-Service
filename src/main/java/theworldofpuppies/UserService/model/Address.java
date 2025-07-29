package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private AddressType addressType;
    private String contactNumber;
    private String contactName;
    private String houseNumber;
    private String street;
    private String landmark;
    private String City;
    private String state;
    private String pinCode;
    private String country;
}
