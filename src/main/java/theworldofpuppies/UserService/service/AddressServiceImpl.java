package theworldofpuppies.UserService.service;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.Address;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    @Override
    public UpdateResult setProductField() {
        Query query = new Query();
        Update update = new Update().set("addresses", new ArrayList<String>());
        return mongoTemplate.updateMulti(query, update, User.class);
    }

    @Override
    public Address setAddress(String userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.getAddresses().add(address);
        userRepository.save(user);
        return address;
    }

    @Override
    public List<Address> getAddresses(String userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getAddresses().isEmpty()) {
            throw new ResourceNotFoundException("no address found for this user");
        }
        return user.getAddresses();
    }

}
