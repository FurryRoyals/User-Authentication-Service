package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "address")
public class Address {
    @Id
    private String id;
    private String userId;
    private AddressType addressType;
    private String contactNumber;
    private String contactName;
    private String houseNumber;
    private String street;
    private String landmark;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private Boolean isSelected = false;

    public Address(String userId,
                   AddressType addressType,
                   String contactNumber,
                   String contactName,
                   String houseNumber,
                   String street,
                   String landmark,
                   String city,
                   String state,
                   String pinCode,
                   String country) {
        this.userId = userId;
        this.addressType = addressType;
        this.contactNumber = contactNumber;
        this.contactName = contactName;
        this.houseNumber = houseNumber;
        this.street = street;
        this.landmark = landmark;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.country = country;
    }
}
