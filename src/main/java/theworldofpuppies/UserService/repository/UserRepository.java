package theworldofpuppies.UserService.repository;

import theworldofpuppies.UserService.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    User findByEmail(String email);
    List<User> findByIdIn(List<String> ids);
}
