
package com.smartlms.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.EnrollmentStatus;
import com.smartlms.entity.Role;

import com.smartlms.repository.UserRepository;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.EnrollmentRepository;
import com.smartlms.service.EnrollmentService;

@Controller
@RequestMapping("/admin/enrollments")
public class AdminEnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    public AdminEnrollmentController(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentService enrollmentService) {

        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
    }

    // =====================================
    // ENROLLMENT LIST
    // =====================================

    @GetMapping
    public String enrollments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EnrollmentStatus status,
            Model model) {

        List<Enrollment> allEnrollments =
                enrollmentRepository.findAll();

        // Search Filter
        List<Enrollment> filteredEnrollments =
                allEnrollments.stream()
                .filter(e -> {

                    if (keyword == null || keyword.isBlank()) {
                        return true;
                    }

                    String search = keyword
                            .trim()
                            .toLowerCase(Locale.ROOT);

                    String studentName = e.getStudent()
                            .getName()
                            .toLowerCase(Locale.ROOT);

                    String studentEmail = e.getStudent()
                            .getEmail()
                            .toLowerCase(Locale.ROOT);

                    String courseTitle = e.getCourse()
                            .getTitle()
                            .toLowerCase(Locale.ROOT);

                    return studentName.contains(search)
                            || studentEmail.contains(search)
                            || courseTitle.contains(search);
                })
                .filter(e ->
                        status == null || e.getStatus() == status)
                .toList();

        // Statistics
        long totalEnrollments = allEnrollments.size();

        long activeEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        long pendingEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING)
                .count();

        long completedEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        long cancelledEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.CANCELLED)
                .count();

        // Dropdown Data
        List<AppUser> students =
                userRepository.findByRole(Role.STUDENT);

        List<Course> courses =
                courseRepository.findByActiveTrue();

        model.addAttribute("enrollments", filteredEnrollments);

        model.addAttribute("totalEnrollments", totalEnrollments);
        model.addAttribute("activeEnrollments", activeEnrollments);
        model.addAttribute("pendingEnrollments", pendingEnrollments);
        model.addAttribute("completedEnrollments", completedEnrollments);
        model.addAttribute("cancelledEnrollments", cancelledEnrollments);

        model.addAttribute("students", students);
        model.addAttribute("courses", courses);

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("enrollmentStatuses", EnrollmentStatus.values());

        return "admin/enrollments";
    }

    // =====================================
    // CREATE NEW ENROLLMENT
    // =====================================

    @PostMapping("/create")
    public String createEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            RedirectAttributes redirectAttributes) {

        try {

            enrollmentService.enrollStudent(studentId, courseId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Student enrolled successfully!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/enrollments";
    }

    // =====================================
    // UPDATE ENROLLMENT STATUS
    // =====================================

    @PostMapping("/status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam EnrollmentStatus status,
            RedirectAttributes redirectAttributes) {

        try {

            Enrollment enrollment = enrollmentRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Enrollment not found."));

            enrollment.setStatus(status);

            enrollmentRepository.save(enrollment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Enrollment status updated successfully!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/enrollments";
    }

    // =====================================
    // DELETE ENROLLMENT
    // =====================================

    @PostMapping("/delete/{id}")
    public String deleteEnrollment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            Enrollment enrollment = enrollmentRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Enrollment not found."));

            enrollmentRepository.delete(enrollment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Enrollment deleted successfully!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/enrollments";
    }
}
