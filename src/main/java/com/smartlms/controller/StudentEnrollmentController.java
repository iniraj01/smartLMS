package com.smartlms.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.Course;
import com.smartlms.entity.AppUser;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Role;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.CourseService;
import com.smartlms.service.EnrollmentService;

@Controller
@RequestMapping("/student")
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final UserRepository userRepository;

    public StudentEnrollmentController(
            EnrollmentService enrollmentService,
            CourseService courseService,
            UserRepository userRepository) {

        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    // Show all available courses
    @GetMapping("/available-courses")
    public String availableCourses(Model model, Principal principal) {

        AppUser student = getStudent(principal);

        List<Course> courses = courseService.getActiveCourses();

        List<Enrollment> enrollments =
                enrollmentService.getStudentEnrollments(student.getId());

        List<Long> enrolledCourseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .toList();

        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        return "student/available-courses";
    }

    // Enroll in a course
    @PostMapping("/enroll/{courseId}")
    public String enrollCourse(
            @PathVariable Long courseId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        AppUser student = getStudent(principal);

        try {
            enrollmentService.enrollStudent(
                    student.getId(), courseId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "You have successfully enrolled in the course!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/student/available-courses";
    }

    // Show student's enrolled courses
    @GetMapping("/my-courses")
    public String myCourses(Model model, Principal principal) {

        AppUser student = getStudent(principal);

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalCourses", enrollments.size());

        return "student/my-courses";
    }

    // Get logged-in student
    private AppUser getStudent(Principal principal) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found!"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can access this page!");
        }

        return student;
    }
}