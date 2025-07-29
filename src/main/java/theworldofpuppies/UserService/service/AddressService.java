package theworldofpuppies.UserService.service;


import com.mongodb.client.result.UpdateResult;
import theworldofpuppies.UserService.model.Address;

import java.util.List;

public interface AddressService {
    UpdateResult setProductField();
    Address setAddress(String userId, Address address);
    List<Address> getAddresses(String userId);
}
