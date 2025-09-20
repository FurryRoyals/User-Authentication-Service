package theworldofpuppies.UserService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import theworldofpuppies.UserService.model.Address;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByUserId(String userId);
    Optional<Address> findByUserIdAndIsSelected(String userId, Boolean isSelected);

}
