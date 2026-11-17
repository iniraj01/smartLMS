
package com.smartlms.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Assignment;
import com.smartlms.entity.Role;
import com.smartlms.repository.AssignmentRepository;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.AssignmentSubmissionService;
import com.smartlms.service.EnrollmentService;

@Controller
@RequestMapping("/student")
public class StudentAssignmentSubmissionController {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AssignmentSubmissionService submissionService;
    private final EnrollmentService enrollmentService;

    public StudentAssignmentSubmissionController(
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            AssignmentSubmissionService submissionService,
            EnrollmentService enrollmentService) {

        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
        this.enrollmentService = enrollmentService;
    }

    // Open assignment submission form
    @GetMapping("/submit-assignment/{id}")
    public String showSubmissionForm(
            @PathVariable Long id,
            Principal principal,
            Model model) {

        AppUser student = getStudent(principal);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found."));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException(
                    "This assignment is not available.");
        }

        boolean enrolled = enrollmentService
                .getActiveEnrollments(student.getId())
                .stream()
                .anyMatch(e -> e.getCourse().getId()
                        .equals(assignment.getCourse().getId()));

        if (!enrolled) {
            throw new IllegalArgumentException(
                    "You are not enrolled in this course.");
        }

        model.addAttribute("assignment", assignment);
        model.addAttribute("studentName", student.getName());

        return "student/submit-assignment";
    }

    // Submit assignment with optional PDF
    @PostMapping("/submit-assignment/{id}")
    public String submitAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String submissionText,
            @RequestParam(required = false) String attachmentUrl,
            @RequestParam(required = false) MultipartFile pdfFile,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            AppUser student = getStudent(principal);

            submissionService.submitAssignment(
                    student.getId(),
                    id,
                    submissionText,
                    attachmentUrl,
                    pdfFile
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Assignment submitted successfully!");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to submit assignment. Please try again.");
        }

        return "redirect:/student/assignments";
    }

    // Validate logged-in student
    private AppUser getStudent(Principal principal) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Access denied.");
        }

        return student;
    }
}
