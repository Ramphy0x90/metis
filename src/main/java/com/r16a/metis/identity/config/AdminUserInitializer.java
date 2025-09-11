package com.r16a.metis.identity.config;

import com.r16a.metis.identity.models.Role;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.models.UserRole;
import com.r16a.metis.identity.repositories.RoleRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:admin@admin.com}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:password}")
    private String adminPassword;

    @EventListener(ApplicationReadyEvent.class)
    private void initializeAdminUser() {
        Optional<User> existingUser = userRepository.findByEmail(adminUsername);
        
        if (existingUser.isPresent()) {
            log.info("Global admin user already exists: {}", adminUsername);
            return;
        }

        Optional<Role> globalAdminRole = roleRepository.findByName(UserRole.GLOBAL_ADMIN);
        if (globalAdminRole.isEmpty()) {
            log.error("Global Admin role not found. Please ensure roles have been initialized.");
            return;
        }

        User adminUser = new User();
        adminUser.setEmail(adminUsername);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setName("System");
        adminUser.setSurname("Administrator");
        adminUser.setTenant(null);
        adminUser.setRoles(Set.of(globalAdminRole.get()));

        userRepository.save(adminUser);
        log.info("Created global admin user: {}", adminUsername + " " + adminPassword);
    }
}
