package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.exception.UnauthorizedException;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApiService {

    @Autowired
    private UserRepository userRepository;

    public boolean validateAdmin(String phoneNumber) {
        // Use Optional for better handling
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isPhoneNumberVerified()) {
            throw new UnauthorizedException("Phone number is not verified");
        }

        if (!user.getRoles().contains("ADMIN")) {
            throw new UnauthorizedException("Unauthorized access!");
        }

        return true;
    }


    public boolean validateUser(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isPhoneNumberVerified()) {
            throw new UnauthorizedException("Phone number is not verified");
        }

        if (!user.getRoles().contains("USER")) {
            throw new UnauthorizedException("Unauthorized access!");
        }

        return true;
    }


    public String getUserId(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this phoneNumber: " + phoneNumber));
        return user.getId();
    }
}
