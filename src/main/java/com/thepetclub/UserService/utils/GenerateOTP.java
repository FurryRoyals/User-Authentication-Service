package com.thepetclub.UserService.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class GenerateOTP {
    public String generateOtp() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1000000));
    }
}
