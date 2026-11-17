
package com.smartlms.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.smartlms.entity.LessonProgress;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Role;
import com.smartlms.entity.Lesson;

import com.smartlms.repository.UserRepository;
import com.smartlms.service.EnrollmentService;
import com.smartlms.service.LessonService;
import com.smartlms.service.LessonProgressService;
@Controller
@RequestMapping("/student")
public class StudentCourseController {

    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final UserRepository userRepository;
    private final LessonProgressService progressService;

    public StudentCourseController(
            EnrollmentService enrollmentService,
            LessonService lessonService,
            UserRepository userRepository,
            LessonProgressService progressService) {

        this.enrollmentService = enrollmentService;
        this.lessonService = lessonService;
        this.userRepository = userRepository;
        this.progressService = progressService;
    }

    // Open enrolled course lessons
    @GetMapping("/course/{courseId}/lessons")
    public String showCourseLessons(
            @PathVariable Long courseId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found!"));

        if (student.getRole() != Role.STUDENT) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Only students can access this page!");

            return "redirect:/student/my-courses";
        }

        // Check whether this student is enrolled in the course
        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        Enrollment enrollment = enrollments.stream()
                .filter(item -> item.getCourse().getId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (enrollment == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You are not enrolled in this course!");

            return "redirect:/student/my-courses";
        }

        Course course = enrollment.getCourse();

        // Get only published lessons
        List<Lesson> lessons =
                lessonService.getActiveLessons(courseId);

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("totalLessons", lessons.size());
        Set<Long> completedLessonIds = progressService
                .getCourseProgress(student.getId(), courseId)
                .stream()
                .filter(LessonProgress::isCompleted)
                .map(progress -> progress.getLesson().getId())
                .collect(Collectors.toSet());

        model.addAttribute("completedLessonIds", completedLessonIds);

        model.addAttribute("completedCount",
                progressService.getCompletedLessons(student.getId(), courseId));

        model.addAttribute("progressPercentage",
                progressService.getCourseProgressPercentage(student.getId(), courseId));
        return "student/course-learning";
    }
    
}
