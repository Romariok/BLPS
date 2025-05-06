package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String email;
    private java.time.LocalDateTime lastActiveTime;
}
