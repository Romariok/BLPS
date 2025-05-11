package itmo.blps.blps.bpms;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itmo.blps.blps.repository.UserRepository;
import itmo.blps.blps.security.JWTUtil;

import java.util.Set;

@Service("jwtTokenValidationDelegate")
public class JwtTokenValidationDelegate implements JavaDelegate {
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String token = (String) execution.getVariable("jwt_token");
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            Set<String> authorities = jwtUtil.getAuthoritiesFromToken(token);
            long userId = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BpmnError("401", "UNAUTHORIZED")).getId();
            execution.setVariable("userId", userId);
            execution.setVariable("authorities", authorities);
        } else {
            throw new BpmnError("401", "UNAUTHORIZED");
        }
    }
}
