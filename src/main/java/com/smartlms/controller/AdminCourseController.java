
package com.smartlms.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.Course;
import com.smartlms.service.CourseService;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Show all courses
    @GetMapping
    public String showCourses(Model model) {

        List<Course> courses = courseService.getAllCourses();

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("publishedCourses",
                courses.stream().filter(Course::isActive).count());
        model.addAttribute("draftCourses",
                courses.stream().filter(course -> !course.isActive()).count());

        return "admin/courses";
    }

    // Show create course form
    @GetMapping("/create")
    public String createCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/create-course";
    }

    // Save new course
    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/courses/create";
        }
    }

    // Show edit course form
    @GetMapping("/edit/{id}")
    public String editCourseForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getCourseById(id));
        return "admin/edit-course";
    }

    // Update course
    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable Long id,
                               @ModelAttribute Course course,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourse(id, course);
            redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/courses/edit/" + id;
        }
    }

    // Activate / Deactivate course
    @PostMapping("/status/{id}")
    public String updateCourseStatus(@PathVariable Long id,
                                     @RequestParam boolean active,
                                     RedirectAttributes redirectAttributes) {
        courseService.updateCourseStatus(id, active);
        redirectAttributes.addFlashAttribute("success", "Course status updated!");
        return "redirect:/admin/courses";
    }

    // Delete course
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}
