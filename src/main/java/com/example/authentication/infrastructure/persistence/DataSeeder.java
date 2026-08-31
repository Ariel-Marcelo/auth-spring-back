package com.example.authentication.infrastructure.persistence;

import com.example.authentication.infrastructure.persistence.entities.Role;
import com.example.authentication.infrastructure.persistence.entities.User;
import com.example.authentication.infrastructure.repositories.RoleRepository;
import com.example.authentication.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting DataSeeder execution...");

        // 1. Seed Roles and Permissions
        Role userRole = roleRepository.findByRole(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .role(Role.RoleName.ROLE_USER)
                                .permissions(Set.of("invoices:read"))
                                .build()
                ));

        Role adminRole = roleRepository.findByRole(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .role(Role.RoleName.ROLE_ADMIN)
                                .permissions(Set.of("invoices:read", "invoices:write", "invoices:delete"))
                                .build()
                ));

        // 2. Seed Test Users
        if (!userRepository.existsByUsername("user")) {
            User testUser = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .roles(Set.of(userRole))
                    .createdAt(Instant.now())
                    .build();

            userRepository.save(testUser);
            log.info("Test user 'user' created with password 'user123'");
        }

        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole))
                    .createdAt(Instant.now())
                    .build();

            userRepository.save(adminUser);
            log.info("Test admin 'admin' created with password 'admin123'");
        }

        log.info("DataSeeder execution finished successfully.");
    }
}
