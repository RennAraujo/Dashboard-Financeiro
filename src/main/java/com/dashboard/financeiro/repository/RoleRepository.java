package com.dashboard.financeiro.repository;

import com.dashboard.financeiro.model.Role;
import com.dashboard.financeiro.model.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
    Boolean existsByName(RoleName name);
}
