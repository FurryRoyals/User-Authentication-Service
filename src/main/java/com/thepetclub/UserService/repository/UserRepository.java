package com.thepetclub.UserService.repository;

import com.thepetclub.UserService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(String phoneNumber);
    User findByEmail(String email);
}
