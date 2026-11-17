
package com.smartlms.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Assignment;
import com.smartlms.entity.AssignmentSubmission;
import com.smartlms.entity.Role;
import com.smartlms.entity.SubmissionStatus;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.AssignmentSubmissionService;
import com.smartlms.service.StudentAssignmentService;

@Controller
@RequestMapping("/student")
public class StudentAssignmentController {

    private final StudentAssignmentService studentAssignmentService;
    private final UserRepository userRepository;
    private final AssignmentSubmissionService submissionService;

    public StudentAssignmentController(
            StudentAssignmentService studentAssignmentService,
            UserRepository userRepository,
            AssignmentSubmissionService submissionService) {

        this.studentAssignmentService = studentAssignmentService;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
    }

    // Student Assignment List
    @GetMapping("/assignments")
    public String assignments(Principal principal, Model model) {

        // Find logged-in student
        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found."));

        // Verify student role
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Access denied.");
        }

        // Get student's published assignments
        List<Assignment> assignments =
                studentAssignmentService.getStudentAssignments(student.getId());

        model.addAttribute("assignments", assignments);
        model.addAttribute("studentName", student.getName());

        // Get student's submissions
        List<AssignmentSubmission> studentSubmissions =
                submissionService.getStudentSubmissions(student.getId());

        // Map submissions using Assignment ID
        Map<Long, AssignmentSubmission> submissionMap =
                studentSubmissions.stream()
                        .collect(Collectors.toMap(
                                submission -> submission.getAssignment().getId(),
                                submission -> submission
                        ));

        model.addAttribute("submissionMap", submissionMap);

        // Calculate assignment statistics
        long submitted = studentSubmissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED)
                .count();

        long evaluated = studentSubmissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.EVALUATED)
                .count();

        long pending = assignments.size() - studentSubmissions.size();

        model.addAttribute("totalAssignments", assignments.size());
        model.addAttribute("pendingAssignments", pending);
        model.addAttribute("submittedAssignments", submitted);
        model.addAttribute("evaluatedAssignments", evaluated);

        return "student/assignments";
    }
}
