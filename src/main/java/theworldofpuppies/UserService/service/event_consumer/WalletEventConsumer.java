package theworldofpuppies.UserService.service.event_consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.model.WalletEvent;
import theworldofpuppies.UserService.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletEventConsumer {

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(topics = "wallet.deducted", groupId = "user-service-group")
    public void consumeWalletEvent(WalletEvent event) {
        try {
            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + event.getUserId()));
            double existingBalance = user.getWalletBalance();
            user.setWalletBalance(existingBalance - event.getWalletBalance());
            userRepository.save(user);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
