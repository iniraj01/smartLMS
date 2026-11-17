package com.smartlms.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Role;

public interface UserRepository
        extends JpaRepository<AppUser, Long> {

    // Find user using email
    Optional<AppUser> findByEmail(String email);

    // Check whether email already exists
    boolean existsByEmail(String email);

    // Find users according to role
    List<AppUser> findByRole(Role role);
    List<AppUser> findByRoleOrderByCreatedAtDesc(Role role);
    }