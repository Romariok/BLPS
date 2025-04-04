package itmo.blps.blps.config;

import itmo.blps.blps.model.Permission;
import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.RolePermission;
import itmo.blps.blps.repository.RolePermissionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Component("rolePermissionInitializer")
@RequiredArgsConstructor
@Slf4j
public class RolePermissionInitializer {
    private final RolePermissionRepository rolePermissionRepository;

    @PostConstruct
    @Transactional
    public void initRolePermissions() {
        log.info("Initializing role permissions...");

        // Define permission mappings for each role
        Map<Role, EnumSet<Permission>> rolePermissions = new HashMap<>();

        // Student permissions
        rolePermissions.put(Role.STUDENT, EnumSet.of(
                Permission.VIEW_COURSE,
                Permission.VIEW_TASK,
                Permission.SUBMIT_TASK,
                Permission.VIEW_CERTIFICATE));

        // Teacher permissions
        rolePermissions.put(Role.TEACHER, EnumSet.of(
                Permission.VIEW_COURSE,
                Permission.CREATE_COURSE,
                Permission.EDIT_COURSE,
                Permission.DELETE_COURSE,
                Permission.VIEW_TASK,
                Permission.CREATE_TASK,
                Permission.EDIT_TASK,
                Permission.DELETE_TASK,
                Permission.GRADE_TASK,
                Permission.VIEW_CERTIFICATE,
                Permission.ISSUE_CERTIFICATE));

        // For each role and its permissions
        rolePermissions.forEach((role, permissions) -> {
            permissions.forEach(permission -> {
                // Check if this role-permission combination already exists
                if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
                    // Create and save the new role-permission combination
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Added permission {} to role {}", permission, role);
                }
            });
        });

        log.info("Role permissions initialized successfully!");
    }
}