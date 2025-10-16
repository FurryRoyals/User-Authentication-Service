package theworldofpuppies.UserService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import theworldofpuppies.UserService.model.Referral;

import java.util.List;

@Repository
public interface ReferralRepository extends MongoRepository<Referral, String> {
    List<Referral> findByReferrerIdIn(String referrerId);
}
