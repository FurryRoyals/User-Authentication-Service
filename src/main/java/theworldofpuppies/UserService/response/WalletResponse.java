package theworldofpuppies.UserService.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletResponse {
    private String message;
    private boolean isVerified;
    private Double balance;
}
