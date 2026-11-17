
package com.smartlms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.Role;
import com.smartlms.service.TeacherCourseService;
import com.smartlms.service.UserService;
import com.smartlms.service.CourseService;

@Controller
@RequestMapping("/admin/assignments")
public class AdminTeacherAssignmentController {

    private final TeacherCourseService teacherCourseService;
    private final UserService userService;
    private final CourseService courseService;

    public AdminTeacherAssignmentController(
            TeacherCourseService teacherCourseService,
            UserService userService,
            CourseService courseService) {

        this.teacherCourseService = teacherCourseService;
        this.userService = userService;
        this.courseService = courseService;
    }

    // Show assignment page
    @GetMapping
    public String showAssignmentPage(Model model) {

        model.addAttribute("teachers",
                userService.getUsersByRole(Role.TEACHER));

        model.addAttribute("courses",
                courseService.getAllCourses());

        return "admin/assign-teacher";
    }

    // Assign teacher to course
    @PostMapping("/assign")
    public String assignTeacher(
            @RequestParam Long teacherId,
            @RequestParam Long courseId,
            RedirectAttributes redirectAttributes) {

        try {

            teacherCourseService.assignTeacher(courseId, teacherId);

            redirectAttributes.addFlashAttribute(
                    "success", "Course assigned to teacher successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/admin/assignments";
    }
}
