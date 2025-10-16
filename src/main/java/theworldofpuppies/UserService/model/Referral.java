package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "referrals")
public class Referral {
    @Id
    private String id;
    private String referrerId;
    private String referredUserId;
    private String referredUsername;
    private Boolean rewardGiven = false;
}
