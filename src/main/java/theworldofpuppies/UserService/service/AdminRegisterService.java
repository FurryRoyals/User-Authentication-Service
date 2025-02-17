package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRegisterService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public boolean saveNewAdmin(
            String username, String email,
            String phoneNumber, boolean emailVerified,
            boolean phoneNumberVerified, String password) {

        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            return false; // User already exists
        }

        User newUser = new User(
                username,
                email,
                phoneNumber,
                emailVerified,
                phoneNumberVerified,
                passwordEncoder.encode(password),
                LocalDateTime.now(),
                List.of("ADMIN", "USER") // Consider externalizing role constants
        );

        userRepository.save(newUser);
        return true;
    }


    public boolean checkPhoneNumberForVerification(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(User::isPhoneNumberVerified)
                .orElse(false);
    }


}
