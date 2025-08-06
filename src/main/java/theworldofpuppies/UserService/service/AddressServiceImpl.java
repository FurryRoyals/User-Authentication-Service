package theworldofpuppies.UserService.service;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import theworldofpuppies.UserService.exception.AlreadyExistsException;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.Address;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.AddressRepository;
import theworldofpuppies.UserService.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public UpdateResult setProductField() {
        Query query = new Query();
        Update update = new Update().set("isSelected", false);
        return mongoTemplate.updateMulti(query, update, User.class);
    }

    @Override
    public Address addAddress(String userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address normalizedNew = normalize(address);
        normalizedNew.setUserId(userId);

        // Fetch existing addresses of the user
        List<Address> existingAddresses = user.getAddressIds().stream()
                .map(addressRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // Check for exact duplicate (based on business equality, not ID)
        for (Address existing : existingAddresses) {
            if (areAddressesEqual(existing, normalizedNew)) {
                // Already exists: return the existing one (or you could throw if you prefer)
                throw new AlreadyExistsException("Address already exists");
            }
        }

        // If no address exists OR no address is selected, set the new one as selected
        boolean hasSelected = !existingAddresses.isEmpty() &&
                existingAddresses.stream().anyMatch(Address::getIsSelected);

        normalizedNew.setIsSelected(!hasSelected);

        // Not a duplicate; save new address
        Address savedAddress = addressRepository.save(normalizedNew);
        user.getAddressIds().add(savedAddress.getId());
        userRepository.save(user);
        return savedAddress;
    }

    // Business equality: compare relevant fields, ignore id/userId
    private boolean areAddressesEqual(Address a, Address b) {
        return Objects.equals(trimAndNullify(a.getAddressType()), trimAndNullify(b.getAddressType()))
                && Objects.equals(normalizeString(a.getContactNumber()), normalizeString(b.getContactNumber()))
                && Objects.equals(normalizeString(a.getContactName()), normalizeString(b.getContactName()))
                && Objects.equals(normalizeString(a.getHouseNumber()), normalizeString(b.getHouseNumber()))
                && Objects.equals(normalizeString(a.getStreet()), normalizeString(b.getStreet()))
                && Objects.equals(normalizeString(a.getLandmark()), normalizeString(b.getLandmark()))
                && Objects.equals(normalizeString(a.getCity()), normalizeString(b.getCity()))
                && Objects.equals(normalizeString(a.getState()), normalizeString(b.getState()))
                && Objects.equals(normalizeString(a.getPinCode()), normalizeString(b.getPinCode()))
                && Objects.equals(normalizeString(a.getCountry()), normalizeString(b.getCountry()));
    }

    // Example normalizations
    private Address normalize(Address in) {
        Address out = new Address();
        out.setAddressType(in.getAddressType());
        out.setContactNumber(normalizeString(in.getContactNumber()));
        out.setContactName(normalizeString(in.getContactName()));
        out.setHouseNumber(normalizeString(in.getHouseNumber()));
        out.setStreet(normalizeString(in.getStreet()));
        out.setLandmark(normalizeString(in.getLandmark()));
        out.setCity(normalizeString(in.getCity()));
        out.setState(normalizeString(in.getState()));
        out.setPinCode(normalizeString(in.getPinCode()));
        out.setCountry(normalizeString(in.getCountry()));
        // userId and id handled outside
        return out;
    }

    private String normalizeString(String s) {
        return (s == null) ? null : s.trim().replaceAll("\\s+", " ");
    }

    private <T> T trimAndNullify(T t) {
        return t; // placeholder if you need further normalization by type
    }


    @Override
    public List<Address> getAddresses(String userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<String> addressIds = user.getAddressIds();
        if (addressIds.isEmpty()) {
            throw new ResourceNotFoundException("no address found for this user");
        }
        List<Address> addresses = addressRepository.findByUserId(userId);
        if (addresses.isEmpty()) {
            throw new ResourceNotFoundException("no address found for this user");
        }
        return addresses;
    }

    @Override
    public Address updateAddress(String userId,
                                 Address address) throws ResourceNotFoundException {
        Address existingAddress = addressRepository.findById(address.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address doesn't exist with this: " + address.getId()));

        if (!Objects.equals(existingAddress.getUserId(), userId)) {
            throw new ResourceNotFoundException("Address does not belong to user: " + userId);
        }

        boolean changed = false;

        changed |= updateIfChanged(address.getContactNumber(), existingAddress::getContactNumber, existingAddress::setContactNumber);
        changed |= updateIfChanged(address.getAddressType(), existingAddress::getAddressType, existingAddress::setAddressType);
        changed |= updateIfChanged(address.getContactName(), existingAddress::getContactName, existingAddress::setContactName);
        changed |= updateIfChanged(address.getHouseNumber(), existingAddress::getHouseNumber, existingAddress::setHouseNumber);
        changed |= updateIfChanged(address.getStreet(), existingAddress::getStreet, existingAddress::setStreet);
        changed |= updateIfChanged(address.getLandmark(), existingAddress::getLandmark, existingAddress::setLandmark);
        changed |= updateIfChanged(address.getCity(), existingAddress::getCity, existingAddress::setCity);
        changed |= updateIfChanged(address.getState(), existingAddress::getState, existingAddress::setState);
        changed |= updateIfChanged(address.getPinCode(), existingAddress::getPinCode, existingAddress::setPinCode);
        changed |= updateIfChanged(address.getCountry(), existingAddress::getCountry, existingAddress::setCountry);

        if (changed) {
            return addressRepository.save(existingAddress);
        } else {
            return existingAddress;
        }
    }

    @Override
    public List<Address> updateAddressSelection(String userId, String addressId) {
        List<Address> addresses = addressRepository.findByUserId(userId);

        // Optional: Check if the provided addressId exists in the list
        boolean exists = addresses.stream()
                .anyMatch(address -> address.getId().equals(addressId));

        if (!exists) {
            throw new ResourceNotFoundException("Address with ID " + addressId + " not found for user " + userId);
        }

        // Update only if selection has changed
        boolean isChanged = false;
        for (Address address : addresses) {
            boolean shouldBeSelected = address.getId().equals(addressId);
            if (address.getIsSelected() != shouldBeSelected) {
                address.setIsSelected(shouldBeSelected);
                isChanged = true;
            }
        }

        return isChanged ? addressRepository.saveAll(addresses) : addresses;
    }


    @Override
    public List<Address> deleteAddress(String userId, String addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID: " + userId));

        Address addressToDelete = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("No address found with ID: " + addressId));

        // ✅ Make sure address belongs to the user
        if (!addressToDelete.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to the user.");
        }

        // ✅ Delete address from DB
        addressRepository.delete(addressToDelete);

        // ✅ Remove addressId from user's address list
        user.getAddressIds().remove(addressId);
        userRepository.save(user);

        // ✅ Return remaining addresses
        return addressRepository.findByUserId(userId);
    }


    private <T> boolean updateIfChanged(T newVal, Supplier<T> getter, Consumer<T> setter) {
        T oldVal = getter.get();
        if (newVal != null && !Objects.equals(newVal, oldVal)) {
            setter.accept(newVal);
            return true;
        }
        return false;
    }


}
