package itmo.blps.blps.bpms;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import itmo.blps.blps.dto.LoginRequestDTO;
import itmo.blps.blps.service.AuthService;

@Service("authServ")
public class AuthServiceBpms {
    @Autowired
    AuthService authService;
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String auth(String username, String password) {
        try {
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);
            return authService.login(loginRequest).getToken();
        } catch (Exception e) {
            throw new BpmnError("400", "WRONG_CREDITS");
        }
    }
}
