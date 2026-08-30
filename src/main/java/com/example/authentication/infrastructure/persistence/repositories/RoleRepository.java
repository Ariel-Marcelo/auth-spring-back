package com.example.authentication.infrastructure.persistence.repositories;

import com.example.authentication.infrastructure.persistence.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRole(Role.RoleName role);
}
