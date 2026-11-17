package com.smartlms.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartlms.entity.Course;
import com.smartlms.service.TeacherCourseService;

@Controller
public class TeacherController {

    private final TeacherCourseService teacherCourseService;

    public TeacherController(TeacherCourseService teacherCourseService) {
        this.teacherCourseService = teacherCourseService;
    }

    // Teacher Dashboard
    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model, Principal principal) {

        String email = principal.getName();

        Long teacherId =
                teacherCourseService.getTeacherIdByEmail(email);

        List<Course> courses =
                teacherCourseService.getTeacherCoursesByEmail(email);

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());

        model.addAttribute(
                "totalStudents",
                teacherCourseService.getTotalUniqueStudents(courses)
        );

        model.addAttribute(
                "pendingAssignments",
                teacherCourseService.getPendingSubmissionCount(teacherId)
        );

        model.addAttribute(
                "totalLessons",
                teacherCourseService.getTotalActiveLessons(courses)
        );

        model.addAttribute(
                "recentSubmissions",
                teacherCourseService.getRecentSubmissions(teacherId)
        );

        return "teacher/dashboard";
    }

    // Teacher My Courses
    @GetMapping("/teacher/courses")
    public String teacherCourses(Model model, Principal principal) {

        String email = principal.getName();

        List<Course> courses =
                teacherCourseService.getTeacherCoursesByEmail(email);

        long publishedCourses = courses.stream()
                .filter(Course::isActive)
                .count();

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("publishedCourses", publishedCourses);

        model.addAttribute(
                "totalStudents",
                teacherCourseService.getTotalUniqueStudents(courses)
        );

        model.addAttribute(
                "courseStudentCounts",
                teacherCourseService.getCourseStudentCounts(courses)
        );

        model.addAttribute(
                "courseLessonCounts",
                teacherCourseService.getCourseLessonCounts(courses)
        );

        return "teacher/courses";
    }

    @GetMapping("/teacher/profile")
    public String teacherProfile() {
        return "teacher/profile";
    }
}