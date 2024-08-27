package com.thepetclub.UserService.repository;

import com.thepetclub.UserService.model.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    Optional<TemporaryUser> findByPhoneNumber(String phoneNumber);
}


