package theworldofpuppies.UserService.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupVerificationRequest {
    private String phoneNumber;
    private String otp;
    private String referralCode = "";
}
