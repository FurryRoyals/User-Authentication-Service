package theworldofpuppies.UserService.config;

import com.twilio.type.PhoneNumber;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TwilioConfig {

    @Value("${twilio.accountSID}")
    private String accountSID;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String phoneNumber;

}
