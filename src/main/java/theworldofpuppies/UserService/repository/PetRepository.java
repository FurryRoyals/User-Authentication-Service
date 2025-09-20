package theworldofpuppies.UserService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import theworldofpuppies.UserService.model.Pet;

@Repository
public interface PetRepository extends MongoRepository<Pet, String> {
}
