package theworldofpuppies.UserService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import theworldofpuppies.UserService.service.AdminRegisterService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.Map;


@RestController
@Slf4j
@RequestMapping("${prefix}/ADMIN")
@RequiredArgsConstructor
public class AdminController {

    private final PasswordEncoder passwordEncoder;


    private final JwtUtils jwtUtils;

    private final AdminRegisterService adminregisterService;

    @PostMapping("/save")
    public ResponseEntity<?> saveNewAdmin(@RequestBody Map<String, String> admin) {
        try {
            // Extract values from the admin map
            String username = admin.get("username");
            String email = admin.get("email");
            String phoneNumber = admin.get("phoneNumber");
            boolean emailVerified = Boolean.parseBoolean(admin.get("emailVerified"));
            boolean phoneNumberVerified = Boolean.parseBoolean(admin.get("phoneNumberVerified"));
            String password = admin.get("password");

            // Call the service method to save the new admin
            boolean isNewAdmin = adminregisterService.saveNewAdmin(username, email, phoneNumber, emailVerified, phoneNumberVerified, password);
            if (isNewAdmin) {
                return new ResponseEntity<>("Admin saved successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("phoneNumber is already in use", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("An error occurred while saving the admin: ", e);
            return new ResponseEntity<>("An error occurred while saving the admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}



