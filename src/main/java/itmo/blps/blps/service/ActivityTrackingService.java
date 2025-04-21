package itmo.blps.blps.service;

import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityTrackingService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackActivity(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastActiveTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}