
package com.smartlms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.*;
import com.smartlms.repository.AttendanceRepository;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.UserRepository;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TeacherCourseService teacherCourseService;
    private final EnrollmentService enrollmentService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            TeacherCourseService teacherCourseService,
            EnrollmentService enrollmentService) {

        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.teacherCourseService = teacherCourseService;
        this.enrollmentService = enrollmentService;
    }

    // Mark or update attendance
    public Attendance markAttendance(
            Long teacherId,
            Long studentId,
            Long courseId,
            LocalDate date,
            AttendanceStatus status,
            String remarks) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Access denied.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found."));

        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException(
                    "You are not assigned to this course.");
        }

        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Invalid attendance date.");
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Please select attendance status.");
        }

        boolean enrolled = enrollmentService
                .getActiveEnrollments(studentId)
                .stream()
                .anyMatch(e -> e.getCourse().getId().equals(courseId));

        if (!enrolled) {
            throw new IllegalArgumentException(
                    "Student is not enrolled in this course.");
        }

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        Attendance attendance = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendanceDate(
                        studentId, courseId, date)
                .orElseGet(Attendance::new);

        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setRemarks(remarks);

        return attendanceRepository.save(attendance);
    }

    // Get attendance for a course and date
    @Transactional(readOnly = true)
    public List<Attendance> getCourseAttendance(
            Long teacherId, Long courseId, LocalDate date) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found."));

        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Access denied.");
        }

        return attendanceRepository.findByCourseIdAndAttendanceDate(
                courseId, date);
    }

    // Get student's complete attendance history
    @Transactional(readOnly = true)
    public List<Attendance> getStudentAttendance(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    // Get student's attendance for a particular course
    @Transactional(readOnly = true)
    public List<Attendance> getStudentCourseAttendance(
            Long studentId, Long courseId) {

        return attendanceRepository.findByStudentIdAndCourseId(
                studentId, courseId);
    }

    // Calculate attendance percentage
    @Transactional(readOnly = true)
    public double getAttendancePercentage(
            Long studentId, Long courseId) {

        long total = attendanceRepository.countByStudentIdAndCourseId(
                studentId, courseId);

        if (total == 0) {
            return 0.0;
        }

        long present = attendanceRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, AttendanceStatus.PRESENT);

        long late = attendanceRepository.countByStudentIdAndCourseIdAndStatus(
                studentId, courseId, AttendanceStatus.LATE);

        return ((present + late) * 100.0) / total;
    }
}
