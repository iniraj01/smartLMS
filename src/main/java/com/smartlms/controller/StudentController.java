
package com.smartlms.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Attendance;
import com.smartlms.entity.AttendanceStatus;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Role;

import com.smartlms.repository.UserRepository;

import com.smartlms.service.EnrollmentService;
import com.smartlms.service.LessonProgressService;
import com.smartlms.service.CertificateService;
import com.smartlms.service.AttendanceService;

@Controller
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final LessonProgressService progressService;
    private final CertificateService certificateService;
    private final AttendanceService attendanceService;

    public StudentController(
            EnrollmentService enrollmentService,
            UserRepository userRepository,
            LessonProgressService progressService,
            CertificateService certificateService,
            AttendanceService attendanceService) {

        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
        this.progressService = progressService;
        this.certificateService = certificateService;
        this.attendanceService = attendanceService;
    }

    // Student Dashboard
    @GetMapping("/student/dashboard")
    public String studentDashboard(Principal principal, Model model) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Invalid student account");
        }

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        int totalCourses = enrollments.size();
        int completedLessons = 0;
        int totalProgress = 0;

        for (Enrollment enrollment : enrollments) {

            Long courseId = enrollment.getCourse().getId();

            completedLessons += progressService.getCompletedLessons(
                    student.getId(), courseId);

            totalProgress += progressService.getCourseProgressPercentage(
                    student.getId(), courseId);
        }

        int overallProgress = totalCourses > 0
                ? totalProgress / totalCourses
                : 0;

        model.addAttribute("student", student);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("overallProgress", overallProgress);
        model.addAttribute("recentEnrollments",
                enrollments.stream().limit(3).toList());

        return "student/dashboard";
    }

    // Browse Courses
    @GetMapping("/student/courses")
    public String browseCourses() {
        return "student/courses";
    }

    // Student Attendance Dashboard
    @GetMapping("/student/attendance")
    public String attendance(Principal principal, Model model) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Invalid student account");
        }

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        List<Attendance> attendanceList =
                attendanceService.getStudentAttendance(student.getId());

        long present = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long total = attendanceList.size();

        double percentage = total > 0
                ? ((present + late) * 100.0) / total
                : 0.0;

        // Course-wise attendance percentage
        Map<Long, Double> courseAttendanceMap = new HashMap<>();

        for (Enrollment enrollment : enrollments) {

            Long courseId = enrollment.getCourse().getId();

            double coursePercentage =
                    attendanceService.getAttendancePercentage(
                            student.getId(), courseId);

            courseAttendanceMap.put(
                    courseId,
                    Math.round(coursePercentage * 100.0) / 100.0
            );
        }

        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("attendanceList", attendanceList);

        model.addAttribute("totalAttendance", total);
        model.addAttribute("presentDays", present);
        model.addAttribute("absentDays", absent);
        model.addAttribute("lateDays", late);

        model.addAttribute("attendancePercentage",
                Math.round(percentage * 100.0) / 100.0);

        model.addAttribute("courseAttendanceMap", courseAttendanceMap);

        return "student/attendance";
    }

    // Student Progress
    @GetMapping("/student/progress")
    public String progress() {
        return "student/progress";
    }

    // Student Certificates
    @GetMapping("/student/certificates")
    public String certificates(Principal principal, Model model) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(student.getId());

        for (Enrollment enrollment : enrollments) {

            Long courseId = enrollment.getCourse().getId();

            int percentage = progressService.getCourseProgressPercentage(
                    student.getId(), courseId);

            if (percentage == 100) {
                certificateService.generateCertificate(
                        student.getId(), courseId);
            }
        }

        model.addAttribute("certificates",
                certificateService.getStudentCertificates(student.getId()));

        model.addAttribute("student", student);

        return "student/certificates";
    }

    // Student Profile
    @GetMapping("/student/profile")
    public String studentProfile() {
        return "student/profile";
    }
}
