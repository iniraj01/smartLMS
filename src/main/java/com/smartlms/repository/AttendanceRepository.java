
package com.smartlms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.Attendance;
import com.smartlms.entity.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByCourseIdAndAttendanceDate(
            Long courseId, LocalDate attendanceDate);

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByStudentIdAndCourseId(
            Long studentId, Long courseId);

    // Keep only Optional version for one student's attendance on one date
    Optional<Attendance> findByStudentIdAndCourseIdAndAttendanceDate(
            Long studentId, Long courseId, LocalDate attendanceDate);

    long countByStudentIdAndCourseIdAndStatus(
            Long studentId, Long courseId, AttendanceStatus status);

    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseIdAndAttendanceDateAndStatus(
            Long courseId, LocalDate attendanceDate, AttendanceStatus status);
}
