
package com.smartlms.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Role;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.EnrollmentService;
import com.smartlms.service.LessonProgressService;

@Controller
@RequestMapping("/student")
public class StudentProgressController {

    private final LessonProgressService progressService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    public StudentProgressController(
            LessonProgressService progressService,
            EnrollmentService enrollmentService,
            UserRepository userRepository) {

        this.progressService = progressService;
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
    }

    // Mark lesson as completed
    @PostMapping("/lesson/{lessonId}/complete")
    public String markComplete(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser student = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Student not found!"));

            if (student.getRole() != Role.STUDENT) {
                throw new IllegalArgumentException(
                        "Only students can update lesson progress!");
            }

            // Check active enrollment
            boolean enrolled = enrollmentService
                    .getActiveEnrollments(student.getId())
                    .stream()
                    .anyMatch(item ->
                            item.getCourse().getId().equals(courseId));

            if (!enrolled) {
                throw new IllegalArgumentException(
                        "You are not actively enrolled in this course!");
            }

            progressService.markLessonComplete(
                    student.getId(), lessonId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lesson marked as completed!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/student/course/" + courseId + "/lessons";
    }
}
