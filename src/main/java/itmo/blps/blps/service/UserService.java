package itmo.blps.blps.service;

import itmo.blps.blps.model.Permission;
import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.User;
import itmo.blps.blps.model.UserCourseRole;
import itmo.blps.blps.repository.RolePermissionRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Get all roles associated with the user from their courses
        Set<Role> roles = user.getCourseRoles().stream()
                .map(UserCourseRole::getRole)
                .collect(Collectors.toSet());

        // Always add USER role for authenticated users
        roles.add(Role.STUDENT);

        // Collect all permissions for all roles
        Set<Permission> permissions = new HashSet<>();
        for (Role role : roles) {
            permissions.addAll(rolePermissionRepository.findPermissionsByRole(role));
        }

        // Create authorities for both roles and permissions for JAAS
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));

        // Add permission-based authorities
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));

        // Return Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}