
package com.smartlms.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Role;
import com.smartlms.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Student Registration
    public AppUser registerStudent(
            String name,
            String email,
            String password,
            String phone) {

        return createUser(name, email, password, phone, Role.STUDENT);
    }

    // Create Student or Teacher
    public AppUser createUser(
            String name,
            String email,
            String password,
            String phone,
            Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists!");
        }

        if (role == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Admin account cannot be created from this module!");
        }

        AppUser user = new AppUser();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // Get All Users
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    // Get Users By Role
    public List<AppUser> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Get User By ID
    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("User not found!"));
    }

    // Update User
    @Transactional
    public AppUser updateUser(
            Long id,
            String name,
            String email,
            String phone,
            String password) {

        AppUser user = getUserById(id);

        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Email already exists!");
            }
        });

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);

        // Update password only when provided
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(user);
    }

    // Enable / Disable User
    @Transactional
    public AppUser updateUserStatus(Long id, boolean enabled) {

        AppUser user = getUserById(id);
        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    // Delete User
    public void deleteUser(Long id) {

        AppUser user = getUserById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Admin account cannot be deleted!");
        }

        userRepository.delete(user);
    }
}
