package itmo.blps.blps.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserDTO implements Serializable{
    private static final long serialVersionUID = 1L;
    private String username;
    private String email;
    private java.time.LocalDateTime lastActiveTime;
}
