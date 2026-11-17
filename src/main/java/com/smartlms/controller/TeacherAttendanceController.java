
package com.smartlms.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Attendance;
import com.smartlms.entity.AttendanceStatus;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Role;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.AttendanceService;
import com.smartlms.service.EnrollmentService;
import com.smartlms.service.TeacherCourseService;

@Controller
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;
    private final TeacherCourseService teacherCourseService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public TeacherAttendanceController(
            AttendanceService attendanceService,
            TeacherCourseService teacherCourseService,
            EnrollmentService enrollmentService,
            UserRepository userRepository,
            CourseRepository courseRepository) {

        this.attendanceService = attendanceService;
        this.teacherCourseService = teacherCourseService;
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // Open attendance page
    @GetMapping
    public String attendancePage(
            Principal principal,
            Model model,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        AppUser teacher = getTeacher(principal);

        List<Course> courses =
                teacherCourseService.getTeacherCourses(teacher.getId());

        if (date == null) {
            date = LocalDate.now();
        }

        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedDate", date);
        model.addAttribute("attendanceStatuses",
                AttendanceStatus.values());
        model.addAttribute("teacherName", teacher.getName());

        if (courseId != null) {

            Course course = getOwnedCourse(courseId, teacher.getId());

            List<Enrollment> enrollments =
                    enrollmentService.getActiveEnrollmentsForCourse(courseId);

            List<Attendance> attendanceList =
                    attendanceService.getCourseAttendance(
                            teacher.getId(), courseId, date);

            Map<Long, Attendance> attendanceMap =
                    attendanceList.stream()
                            .collect(Collectors.toMap(
                                    a -> a.getStudent().getId(),
                                    a -> a
                            ));

            model.addAttribute("selectedCourse", course);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("attendanceMap", attendanceMap);
        }

        return "teacher/attendance";
    }

    // Save or update attendance
    @PostMapping("/mark")
    public String markAttendance(
            @RequestParam Long courseId,
            @RequestParam Long studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remarks,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            AppUser teacher = getTeacher(principal);

            attendanceService.markAttendance(
                    teacher.getId(),
                    studentId,
                    courseId,
                    date,
                    status,
                    remarks
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Attendance saved successfully!");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/teacher/attendance?courseId="
                + courseId + "&date=" + date;
    }

    // Validate logged-in teacher
    private AppUser getTeacher(Principal principal) {

        AppUser teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Access denied.");
        }

        return teacher;
    }

    // Verify course belongs to logged-in teacher
    private Course getOwnedCourse(Long courseId, Long teacherId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found."));

        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(teacherId)) {

            throw new IllegalArgumentException(
                    "You are not assigned to this course.");
        }

        return course;
    }
}
