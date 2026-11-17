
package com.smartlms.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Exam;
import com.smartlms.entity.Role;
import com.smartlms.repository.ExamRepository;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.EnrollmentService;

@Controller
@RequestMapping("/student")
public class StudentAvailableExamController {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    public StudentAvailableExamController(
            ExamRepository examRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService) {

        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/exams")
    public String availableExams(
            Principal principal,
            Model model) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("Access denied");
        }

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        List<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .distinct()
                .toList();

        List<Exam> exams = courseIds.isEmpty()
                ? List.of()
                : examRepository.findByCourseIdInAndActiveTrue(courseIds);

        model.addAttribute("exams", exams);

        return "student/exams";
    }
}
