package com.thepetclub.UserService.config;

import com.thepetclub.UserService.filter.JwtFilter;
import com.thepetclub.UserService.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private AuthService authService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers("/auth/user/**").permitAll()

                        // Endpoints that can be accessed by users with any role (USER or ADMIN)
                        .requestMatchers("/auth/password/reset").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/auth/password/verify-otp").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/auth/password/send-otp").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/auth/user/set-email").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/auth/user/send-otp-to-email").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/auth/user/update-password").hasAnyRole("USER", "ADMIN")

                        // Admin-specific endpoints
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")

                        // Any other requests
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(authService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
