package com.smartlms.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.Course;
import com.smartlms.entity.Lesson;
import com.smartlms.service.CourseService;
import com.smartlms.service.LessonService;
import com.smartlms.service.TeacherCourseService;

@Controller
@RequestMapping("/teacher/lessons")
public class TeacherLessonController {

    private final LessonService lessonService;
    private final TeacherCourseService teacherCourseService;
    private final CourseService courseService;

    public TeacherLessonController(
            LessonService lessonService,
            TeacherCourseService teacherCourseService,
            CourseService courseService) {

        this.lessonService = lessonService;
        this.teacherCourseService = teacherCourseService;
        this.courseService = courseService;
    }

    // Show teacher's assigned courses and lessons
    @GetMapping
    public String showLessons(Principal principal, Model model) {

        List<Course> courses =
                teacherCourseService.getTeacherCoursesByEmail(principal.getName());

        model.addAttribute("courses", courses);

        if (!courses.isEmpty()) {
            Long courseId = courses.get(0).getId();

            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("lessons",
                    lessonService.getLessonsByCourse(courseId));
        } else {
            model.addAttribute("lessons", List.of());
        }

        return "teacher/lessons";
    }

    // Show lessons of selected course
    @GetMapping("/course/{courseId}")
    public String showCourseLessons(
            @PathVariable Long courseId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        List<Course> courses =
                teacherCourseService.getTeacherCoursesByEmail(principal.getName());

        boolean assigned = courses.stream()
                .anyMatch(course -> course.getId().equals(courseId));

        if (!assigned) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You are not authorized to manage this course!");

            return "redirect:/teacher/lessons";
        }

        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("lessons",
                lessonService.getLessonsByCourse(courseId));

        return "teacher/lessons";
    }

    // Add new lesson
    @PostMapping("/add")
    public String addLesson(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String videoUrl,
            @RequestParam Integer lessonOrder,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        List<Course> courses =
                teacherCourseService.getTeacherCoursesByEmail(principal.getName());

        boolean assigned = courses.stream()
                .anyMatch(course -> course.getId().equals(courseId));

        if (!assigned) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You cannot add lessons to this course!");

            return "redirect:/teacher/lessons";
        }

        try {
            lessonService.addLesson(
                    courseId, title, description, videoUrl, lessonOrder);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Lesson added successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/lessons/course/" + courseId;
    }
 // Show Edit Lesson Page
    @GetMapping("/edit/{lessonId}")
    public String showEditLesson(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAssignedCourse(courseId, principal)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You are not authorized!");

            return "redirect:/teacher/lessons";
        }

        try {
            Lesson lesson = lessonService.getLessonsByCourse(courseId)
                    .stream()
                    .filter(item -> item.getId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Lesson not found!"));

            model.addAttribute("lesson", lesson);
            model.addAttribute("courseId", courseId);

            return "teacher/edit-lesson";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/teacher/lessons/course/" + courseId;
        }
    }


    // Update Existing Lesson
    @PostMapping("/edit/{lessonId}")
    public String editLesson(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String videoUrl,
            @RequestParam Integer lessonOrder,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!isAssignedCourse(courseId, principal)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You are not authorized!");

            return "redirect:/teacher/lessons";
        }

        try {
            // Verify that lesson belongs to selected course
            Lesson existingLesson = lessonService.getLessonsByCourse(courseId)
                    .stream()
                    .filter(item -> item.getId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Lesson not found!"));

            lessonService.updateLesson(
                    existingLesson.getId(),
                    title,
                    description,
                    videoUrl,
                    lessonOrder
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Lesson updated successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/lessons/course/" + courseId;
    }
    // Publish / Unpublish lesson
    @PostMapping("/status/{lessonId}")
    public String updateStatus(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            @RequestParam boolean active,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!isAssignedCourse(courseId, principal)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You are not authorized!");

            return "redirect:/teacher/lessons";
        }

        try {
            Lesson lesson = lessonService.getLessonsByCourse(courseId)
                    .stream()
                    .filter(item -> item.getId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found!"));

            lessonService.updateLessonStatus(lesson.getId(), active);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Lesson status updated!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/lessons/course/" + courseId;
    }

    // Delete lesson
    @PostMapping("/delete/{lessonId}")
    public String deleteLesson(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!isAssignedCourse(courseId, principal)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You are not authorized!");

            return "redirect:/teacher/lessons";
        }

        try {
            Lesson lesson = lessonService.getLessonsByCourse(courseId)
                    .stream()
                    .filter(item -> item.getId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found!"));

            lessonService.deleteLesson(lesson.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Lesson deleted successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/lessons/course/" + courseId;
    }

    // Verify course assignment
    private boolean isAssignedCourse(Long courseId, Principal principal) {

        return teacherCourseService
                .getTeacherCoursesByEmail(principal.getName())
                .stream()
                .anyMatch(course -> course.getId().equals(courseId));
    }
}