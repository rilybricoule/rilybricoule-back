package com.sbsolutions.rilybricoule.repository;


import com.sbsolutions.rilybricoule.entity.Permission;
import com.sbsolutions.rilybricoule.entity.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {

    Optional<Permission> findByPermissionName(PermissionName permissionName);


}
