package theworldofpuppies.UserService.service.addrress;


import com.mongodb.client.result.UpdateResult;
import theworldofpuppies.UserService.model.Address;

import java.util.List;

public interface AddressService {
    UpdateResult setProductField();
    Address addAddress(String userId, Address address);
    List<Address> getAddresses(String userId);
    Address updateAddress(String userId, Address address);
    List<Address> updateAddressSelection(String userId, String addressId);
    List<Address> deleteAddress(String userId, String addressId);
    Address getSelectedAddress(String userId);
}
