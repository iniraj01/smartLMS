package com.smartlms.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.AssignmentSubmission;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.EnrollmentStatus;
import com.smartlms.entity.Role;
import com.smartlms.entity.SubmissionStatus;

import com.smartlms.repository.AssignmentSubmissionRepository;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.EnrollmentRepository;
import com.smartlms.repository.LessonRepository;
import com.smartlms.repository.UserRepository;

@Service
public class TeacherCourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    public TeacherCourseService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            AssignmentSubmissionRepository submissionRepository) {

        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.submissionRepository = submissionRepository;
    }

    // Assign teacher to course
    @Transactional
    public void assignTeacher(Long courseId, Long teacherId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found!"));

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found!"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException(
                    "Selected user is not a teacher!");
        }

        course.setTeacher(teacher);
        courseRepository.save(course);
    }

    // Get courses assigned to a teacher
    @Transactional(readOnly = true)
    public List<Course> getTeacherCourses(Long teacherId) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found!"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException(
                    "Selected user is not a teacher!");
        }

        return courseRepository.findByTeacherId(teacherId);
    }

    // Get courses using logged-in teacher email
    @Transactional(readOnly = true)
    public List<Course> getTeacherCoursesByEmail(String email) {

        AppUser teacher = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found!"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher!");
        }

        return courseRepository.findByTeacherEmail(email);
    }

    // Get teacher ID using email
    @Transactional(readOnly = true)
    public Long getTeacherIdByEmail(String email) {

        AppUser teacher = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found!"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher!");
        }

        return teacher.getId();
    }

    // Course-wise active student counts
    @Transactional(readOnly = true)
    public Map<Long, Long> getCourseStudentCounts(List<Course> courses) {

        Map<Long, Long> studentCounts = new HashMap<>();

        for (Course course : courses) {

            long count = enrollmentRepository.countByCourseIdAndStatus(
                    course.getId(), EnrollmentStatus.ACTIVE);

            studentCounts.put(course.getId(), count);
        }

        return studentCounts;
    }

    // Course-wise active lesson counts
    @Transactional(readOnly = true)
    public Map<Long, Long> getCourseLessonCounts(List<Course> courses) {

        Map<Long, Long> lessonCounts = new HashMap<>();

        for (Course course : courses) {

            long count = lessonRepository
                    .findByCourseIdAndActiveTrueOrderByLessonOrderAsc(
                            course.getId())
                    .size();

            lessonCounts.put(course.getId(), count);
        }

        return lessonCounts;
    }

    // Total unique active students across teacher's courses
    @Transactional(readOnly = true)
    public long getTotalUniqueStudents(List<Course> courses) {

        Set<Long> studentIds = new HashSet<>();

        for (Course course : courses) {

            List<Enrollment> enrollments =
                    enrollmentRepository.findByCourseId(course.getId());

            for (Enrollment enrollment : enrollments) {

                if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                    studentIds.add(enrollment.getStudent().getId());
                }
            }
        }

        return studentIds.size();
    }

    // Total active lessons across teacher's courses
    @Transactional(readOnly = true)
    public long getTotalActiveLessons(List<Course> courses) {

        long total = 0;

        for (Course course : courses) {

            total += lessonRepository
                    .findByCourseIdAndActiveTrueOrderByLessonOrderAsc(
                            course.getId())
                    .size();
        }

        return total;
    }

    // Pending submissions waiting for evaluation
    @Transactional(readOnly = true)
    public long getPendingSubmissionCount(Long teacherId) {

        return submissionRepository
                .findByAssignment_Teacher_Id(teacherId)
                .stream()
                .filter(submission ->
                        submission.getStatus() == SubmissionStatus.SUBMITTED)
                .count();
    }

    // Latest five submissions of logged-in teacher
    @Transactional(readOnly = true)
    public List<AssignmentSubmission> getRecentSubmissions(Long teacherId) {

        List<AssignmentSubmission> submissions =
                submissionRepository.findByAssignment_Teacher_Id(teacherId);

        List<AssignmentSubmission> recent = submissions.stream()
                .sorted(
                    Comparator.comparing(
                        AssignmentSubmission::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                    )
                )
                .limit(5)
                .toList();

        // Initialize lazy relationships before leaving transaction
        for (AssignmentSubmission submission : recent) {

            submission.getStudent().getName();
            submission.getAssignment().getTitle();
        }

        return recent;
    }
}