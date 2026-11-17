
package com.smartlms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.Role;
import com.smartlms.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Show All Users
    @GetMapping
    public String showUsers(Model model) {

        model.addAttribute("users", userService.getAllUsers());

        return "admin/users";
    }

    // Show Create User Form
    @GetMapping("/create")
    public String createUserForm(Model model) {

        model.addAttribute("roles", Role.values());

        return "admin/create-user";
    }

    // Save New User
    @PostMapping("/create")
    public String createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam Role role,
            RedirectAttributes redirectAttributes) {

        try {

            userService.createUser(
                    name, email, password, phone, role);

            redirectAttributes.addFlashAttribute(
                    "success", "User created successfully!");

            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());

            return "redirect:/admin/users/create";
        }
    }

    // Delete User
    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute(
                    "success", "User deleted successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Enable / Disable User
    @PostMapping("/status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes) {

        userService.updateUserStatus(id, enabled);

        redirectAttributes.addFlashAttribute(
                "success", "User status updated successfully!");

        return "redirect:/admin/users";
    }
    
 // Show Edit User Form
    @GetMapping("/edit/{id}")
    public String editUserForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("user", userService.getUserById(id));

        return "admin/edit-user";
    }

    // Update User
    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        try {

            userService.updateUser(
                    id, name, email, phone, password);

            redirectAttributes.addFlashAttribute(
                    "success", "User updated successfully!");

            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());

            return "redirect:/admin/users/edit/" + id;
        }
    }
}
