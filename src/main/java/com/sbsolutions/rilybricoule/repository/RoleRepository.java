package com.sbsolutions.rilybricoule.repository;

import com.sbsolutions.rilybricoule.entity.Role;
import com.sbsolutions.rilybricoule.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleName roleName);
}
