package com.thepetclub.UserService;

import com.thepetclub.UserService.config.TwilioConfig;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.thepetclub")
@EnableConfigurationProperties
public class UserServiceApplication {

	@Autowired
	private TwilioConfig twilioConfig;

	@PostConstruct
	public void setUp() {
		Twilio.init(twilioConfig.getAccountSID(), twilioConfig.getAuthToken());
	}

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
