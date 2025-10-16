package theworldofpuppies.UserService.service.referral;

import theworldofpuppies.UserService.model.Referral;

import java.util.List;

public interface ReferralService {

    void createReferral(String referralCode, String referredUserId);

    List<Referral> getReferral(String referrerId);

    String generateReferralCode(String userId);
    Double getWalletBalance(String userId);
}
