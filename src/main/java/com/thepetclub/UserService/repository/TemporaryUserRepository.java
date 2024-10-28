package com.thepetclub.UserService.repository;

import com.thepetclub.UserService.model.TemporaryUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporaryUserRepository extends MongoRepository<TemporaryUser, String> {
    Optional<TemporaryUser> findByPhoneNumber(String phoneNumber);
}


