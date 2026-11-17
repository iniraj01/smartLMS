
package com.smartlms.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Assignment;
import com.smartlms.entity.Role;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.AssignmentService;
import com.smartlms.service.TeacherCourseService;

@Controller
@RequestMapping("/teacher/assignments")
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;
    private final TeacherCourseService teacherCourseService;
    private final UserRepository userRepository;

    public TeacherAssignmentController(
            AssignmentService assignmentService,
            TeacherCourseService teacherCourseService,
            UserRepository userRepository) {

        this.assignmentService = assignmentService;
        this.teacherCourseService = teacherCourseService;
        this.userRepository = userRepository;
    }

    // Assignment List
    @GetMapping
    public String assignments(Principal principal, Model model) {

        AppUser teacher = getLoggedInTeacher(principal);

        // Teacher ke assignments
        model.addAttribute("assignments",
                assignmentService.getTeacherAssignments(teacher.getId()));

        // Teacher ke assigned courses
        model.addAttribute("courses",
                teacherCourseService.getTeacherCourses(teacher.getId()));

        return "teacher/assignments";
    }

    // Create Assignment Form
    @GetMapping("/create")
    public String createForm(Principal principal, Model model) {

        AppUser teacher = getLoggedInTeacher(principal);

        model.addAttribute("courses",
                teacherCourseService.getTeacherCourses(teacher.getId()));

        model.addAttribute("assignment", new Assignment());

        return "teacher/create-assignment";
    }

    // Save Assignment
    @PostMapping("/add")
    public String addAssignment(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam LocalDate dueDate,
            @RequestParam Double totalMarks,
            @RequestParam Long courseId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getLoggedInTeacher(principal);

            assignmentService.createAssignment(
                    title,
                    description,
                    dueDate,
                    totalMarks,
                    courseId,
                    teacher.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Assignment created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/assignments";
    }

    // Edit Assignment Form
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getLoggedInTeacher(principal);

            Assignment assignment = assignmentService
                    .getTeacherAssignments(teacher.getId())
                    .stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Assignment not found."));

            model.addAttribute("assignment", assignment);
            model.addAttribute("courses",
                    teacherCourseService.getTeacherCourses(teacher.getId()));

            return "teacher/edit-assignment";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/teacher/assignments";
        }
    }

    // Update Assignment
    @PostMapping("/update/{id}")
    public String updateAssignment(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam LocalDate dueDate,
            @RequestParam Double totalMarks,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getLoggedInTeacher(principal);

            assignmentService.updateAssignment(
                    id,
                    teacher.getId(),
                    title,
                    description,
                    dueDate,
                    totalMarks);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Assignment updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/assignments";
    }

    // Publish / Unpublish
    @PostMapping("/status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getLoggedInTeacher(principal);

            assignmentService.updateAssignmentStatus(
                    id, teacher.getId(), active);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Assignment status updated!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/assignments";
    }

    // Delete Assignment
    @PostMapping("/delete/{id}")
    public String deleteAssignment(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getLoggedInTeacher(principal);

            assignmentService.deleteAssignment(id, teacher.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Assignment deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/assignments";
    }

    // Logged-in Teacher Validation
    private AppUser getLoggedInTeacher(Principal principal) {

        if (principal == null) {
            throw new IllegalArgumentException("Please login first.");
        }

        AppUser teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Access denied.");
        }

        return teacher;
    }
}
