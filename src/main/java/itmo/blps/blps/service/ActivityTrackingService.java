package itmo.blps.blps.service;

import itmo.blps.blps.model.User;
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

    /**
     * Updates the last activity time for a user
     * 
     * @param userId The ID of the user whose activity to track
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackActivity(Long userId) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActiveTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * Updates the last activity time for a user
     * 
     * @param user The user whose activity to track
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackActivity(User user) {
        if (user == null) {
            return;
        }
        user.setLastActiveTime(LocalDateTime.now());
        userRepository.save(user);
    }
}