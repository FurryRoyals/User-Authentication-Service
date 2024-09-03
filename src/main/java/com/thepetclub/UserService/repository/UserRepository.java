package com.thepetclub.UserService.repository;

import com.thepetclub.UserService.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByPhoneNumber(String phoneNumber);
    User findByEmail(String email);
}
