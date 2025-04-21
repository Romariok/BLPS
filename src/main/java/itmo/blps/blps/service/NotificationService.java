package itmo.blps.blps.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void notificateUser(){
        
    }

    @Scheduled(fixedRate = 120000) // 2 minutes
    public void notificateTeacher(){

    }
}
