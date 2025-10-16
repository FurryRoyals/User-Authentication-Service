package theworldofpuppies.UserService.service.referral;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.Referral;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.ReferralRepository;
import theworldofpuppies.UserService.repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;

    @Override
    public void createReferral(String referralCode, String referredUserId) {
        Optional<User> optionalReferrer = userRepository.findByReferralCode(referralCode);
        if (optionalReferrer.isPresent()) {
            User referrer = optionalReferrer.get();
            Optional<User> optionalReferredUser = userRepository.findById(referredUserId);
            if (optionalReferredUser.isPresent()) {
                User referredUser = optionalReferredUser.get();
                referrer.setWalletBalance(referrer.getWalletBalance() + 150.0);
                referredUser.setWalletBalance(referredUser.getWalletBalance() + 150.0);

                Referral referral = new Referral();
                referral.setReferrerId(referrer.getId());
                referral.setReferredUserId(referredUser.getId());
                referral.setReferredUsername(referredUser.getUsername());
                referral.setRewardGiven(true);
                referralRepository.save(referral);
            }
        }
    }

    @Override
    public List<Referral> getReferral(String referrerId) {
        return referralRepository.findByReferrerIdIn(referrerId);
    }


    @Override
    public String generateReferralCode(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String code = user.getReferralCode();

            if (code != null && !code.isEmpty()) {
                return code;
            }

            String prefix = userId.substring(0, Math.min(4, userId.length())).toUpperCase();
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            SecureRandom random = new SecureRandom();

            String newCode;
            do {
                StringBuilder suffix = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    suffix.append(chars.charAt(random.nextInt(chars.length())));
                }
                newCode = prefix + suffix;
            } while (userRepository.existsByReferralCode(newCode));

            user.setReferralCode(newCode);
            userRepository.save(user);

            return newCode;
        }

        throw new ResourceNotFoundException("User not found with ID: " + userId);
    }

    @Override
    public Double getWalletBalance(String userId) {
        return userRepository.findById(userId).orElseThrow
                (() -> new ResourceNotFoundException("No user found with this id: " + userId))
                .getWalletBalance();
    }

}
