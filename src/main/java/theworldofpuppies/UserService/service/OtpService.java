package theworldofpuppies.UserService.service;

import theworldofpuppies.UserService.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final TwilioConfig twilioConfig;

    private final JavaMailSender javaMailSender;

    public void sendOtpToPhoneNumber(String phoneNumber, String otp) {
        PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumber());
        PhoneNumber to = new PhoneNumber(phoneNumber);
        String otpMessage = "Dear customer, your otp is: " + otp + " valid for 10 minutes.";
        Message.creator(
                to, from, otpMessage
        ).create();
        log.info(otpMessage);
    }

    public void sendOtpToEmail(String to, String otp) {
        try {
            String otpMessage = "Dear customer, your otp is: " + otp + "valid for 10 minutes.";
            String subject = "Email verification";
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(otpMessage);
            javaMailSender.send(mail);
        } catch (Exception e) {
            log.error("Exception while sending mail ", e);
        }
    }
}
