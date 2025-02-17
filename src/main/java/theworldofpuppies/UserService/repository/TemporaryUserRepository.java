package theworldofpuppies.UserService.repository;

import theworldofpuppies.UserService.model.TemporaryUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporaryUserRepository extends MongoRepository<TemporaryUser, String> {
    Optional<TemporaryUser> findByPhoneNumber(String phoneNumber);
}


