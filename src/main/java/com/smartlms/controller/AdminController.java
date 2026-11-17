
package com.smartlms.controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Attendance;
import com.smartlms.entity.AttendanceStatus;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.EnrollmentStatus;
import com.smartlms.entity.Role;

import com.smartlms.repository.UserRepository;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.EnrollmentRepository;
import com.smartlms.repository.AttendanceRepository;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    public AdminController(
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            AttendanceRepository attendanceRepository) {

        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    // =========================================
    // ADMIN DASHBOARD
    // =========================================

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        // User Statistics
        long totalStudents =
                userRepository.findByRole(Role.STUDENT).size();

        long totalTeachers =
                userRepository.findByRole(Role.TEACHER).size();

        // Course Statistics
        List<Course> courses = courseRepository.findAll();

        long totalCourses = courses.size();

        long activeCourses =
                courseRepository.findByActiveTrue().size();

        // Enrollment Statistics
        List<Enrollment> enrollments =
                enrollmentRepository.findAll();

        long totalEnrollments = enrollments.size();

        long activeEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        // Attendance Statistics
        long totalAttendance = attendanceRepository.count();

        // Recent Students - Latest 5
        List<AppUser> recentStudents =
                userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT)
                        .stream()
                        .limit(5)
                        .toList();

        // Recent Courses - Latest 5
        List<Course> recentCourses = courses.stream()
                .sorted((a, b) ->
                        b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();

        // Send Data to Thymeleaf
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("activeCourses", activeCourses);
        model.addAttribute("totalEnrollments", totalEnrollments);
        model.addAttribute("activeEnrollments", activeEnrollments);
        model.addAttribute("totalAttendance", totalAttendance);

        model.addAttribute("recentStudents", recentStudents);
        model.addAttribute("recentCourses", recentCourses);

        return "admin/dashboard";
    }

    // =========================================
    // MANAGE STUDENTS
    // =========================================

    @GetMapping("/admin/students")
    public String manageStudents() {
        return "admin/students";
    }

    // =========================================
    // MANAGE TEACHERS
    // =========================================

    @GetMapping("/admin/teachers")
    public String manageTeachers() {
        return "admin/teachers";
    }

    // =========================================
    // MANAGE ENROLLMENTS
    // =========================================



    // =========================================
    // REPORTS AND ANALYTICS
    // =========================================

    @GetMapping("/admin/reports")
    public String reports(Model model) {

        // 1. User Statistics
        List<AppUser> students =
                userRepository.findByRole(Role.STUDENT);

        List<AppUser> teachers =
                userRepository.findByRole(Role.TEACHER);

        long totalStudents = students.size();
        long totalTeachers = teachers.size();

        // 2. Course Statistics
        long totalCourses = courseRepository.count();

        // 3. Enrollment Statistics
        List<Enrollment> enrollmentList =
                enrollmentRepository.findAll();

        long totalEnrollments = enrollmentList.size();

        // 4. Attendance Statistics
        List<Attendance> attendanceList =
                attendanceRepository.findAll();

        long presentCount = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absentCount = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long lateCount = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long totalAttendance = attendanceList.size();

        double attendancePercentage = totalAttendance > 0
                ? ((presentCount + lateCount) * 100.0) / totalAttendance
                : 0.0;

        // 5. Monthly Enrollment Analytics
        Map<String, Long> monthlyEnrollments =
                new LinkedHashMap<>();

        YearMonth currentMonth = YearMonth.now();

        DateTimeFormatter monthFormatter =
                DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {

            YearMonth month = currentMonth.minusMonths(i);

            long count = enrollmentList.stream()
                    .filter(enrollment ->
                            enrollment.getEnrollmentDate() != null
                            && YearMonth.from(enrollment.getEnrollmentDate())
                                    .equals(month))
                    .count();

            monthlyEnrollments.put(
                    month.format(monthFormatter),
                    count
            );
        }

        long maxMonthlyEnrollments =
                monthlyEnrollments.values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0);

        // 6. Popular Courses Analytics
        Map<Course, Long> popularCourses =
                new LinkedHashMap<>();

        courseRepository.findAll()
                .stream()
                .map(course -> Map.entry(
                        course,
                        enrollmentList.stream()
                                .filter(enrollment ->
                                        enrollment.getCourse() != null
                                        && enrollment.getCourse().getId()
                                                .equals(course.getId()))
                                .count()
                ))
                .sorted(Map.Entry.<Course, Long>comparingByValue()
                        .reversed())
                .limit(5)
                .forEach(entry ->
                        popularCourses.put(
                                entry.getKey(),
                                entry.getValue()
                        )
                );

        long maxPopularCourseEnrollments =
                popularCourses.values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0);

        // 7. Send Data to Thymeleaf
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalEnrollments", totalEnrollments);

        model.addAttribute("totalAttendance", totalAttendance);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("lateCount", lateCount);

        model.addAttribute(
                "attendancePercentage",
                String.format("%.1f", attendancePercentage)
        );

        model.addAttribute("monthlyEnrollments", monthlyEnrollments);
        model.addAttribute("maxMonthlyEnrollments", maxMonthlyEnrollments);

        model.addAttribute("popularCourses", popularCourses);
        model.addAttribute(
                "maxPopularCourseEnrollments",
                maxPopularCourseEnrollments
        );

        return "admin/reports";
    }

    // =========================================
    // ADMIN SETTINGS
    // =========================================

    @GetMapping("/admin/settings")
    public String settings() {
        return "admin/settings";
    }
}
