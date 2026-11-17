
package com.smartlms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Assignment;
import com.smartlms.entity.AssignmentSubmission;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.Role;
import com.smartlms.entity.SubmissionStatus;
import com.smartlms.repository.AssignmentRepository;
import com.smartlms.repository.AssignmentSubmissionRepository;
import com.smartlms.repository.UserRepository;

@Service
@Transactional
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final FileStorageService fileStorageService;

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            FileStorageService fileStorageService) {

        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.fileStorageService = fileStorageService;
    }

    // Existing method retained for compatibility
    public AssignmentSubmission submitAssignment(
            Long studentId,
            Long assignmentId,
            String submissionText,
            String attachmentUrl) {

        return submitAssignment(
                studentId, assignmentId, submissionText, attachmentUrl, null);
    }

    // Student submits assignment with optional PDF
    public AssignmentSubmission submitAssignment(
            Long studentId,
            Long assignmentId,
            String submissionText,
            String attachmentUrl,
            MultipartFile pdfFile) {

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can submit assignments.");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found."));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException(
                    "This assignment is not published.");
        }

        // Check active course enrollment
        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(studentId);

        boolean enrolled = enrollments.stream()
                .anyMatch(enrollment ->
                        enrollment.getCourse().getId()
                                .equals(assignment.getCourse().getId()));

        if (!enrolled) {
            throw new IllegalArgumentException(
                    "You are not enrolled in this course.");
        }

        // Check duplicate submission
        if (submissionRepository.existsByStudentIdAndAssignmentId(
                studentId, assignmentId)) {

            throw new IllegalArgumentException(
                    "You have already submitted this assignment.");
        }

        boolean hasText = submissionText != null
                && !submissionText.isBlank();

        boolean hasAttachment = attachmentUrl != null
                && !attachmentUrl.isBlank();

        boolean hasPdf = pdfFile != null && !pdfFile.isEmpty();

        if (!hasText && !hasAttachment && !hasPdf) {
            throw new IllegalArgumentException(
                    "Please provide an answer, attachment link or PDF file.");
        }

        AssignmentSubmission submission = new AssignmentSubmission();

        submission.setStudent(student);
        submission.setAssignment(assignment);
        submission.setSubmissionText(submissionText);
        submission.setAttachmentUrl(attachmentUrl);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        // Save PDF if provided
        if (hasPdf) {

            String storedFilename = fileStorageService.storePdf(pdfFile);

            submission.setOriginalFileName(
                    pdfFile.getOriginalFilename());

            submission.setStoredFileName(storedFilename);
            submission.setFileSize(pdfFile.getSize());
        }

        try {

            return submissionRepository.save(submission);

        } catch (RuntimeException ex) {

            // Remove uploaded file if database save fails
            if (submission.getStoredFileName() != null) {
                fileStorageService.deleteFile(
                        submission.getStoredFileName());
            }

            throw ex;
        }
    }

    // Get submissions made by a student
    @Transactional(readOnly = true)
    public List<AssignmentSubmission> getStudentSubmissions(Long studentId) {

        return submissionRepository.findByStudentId(studentId);
    }

    // Get submissions received by a teacher
    @Transactional(readOnly = true)
    public List<AssignmentSubmission> getTeacherSubmissions(Long teacherId) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher.");
        }

        return submissionRepository.findByAssignment_Teacher_Id(teacherId);
    }

    // Securely get one submission for its assigned teacher
    @Transactional(readOnly = true)
    public AssignmentSubmission getTeacherSubmission(
            Long submissionId,
            Long teacherId) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher.");
        }

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Submission not found."));

        Long assignmentId = submission.getAssignment().getId();

        assignmentRepository.findByIdAndTeacherId(
                assignmentId, teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "You are not authorized to access this submission."));

        return submission;
    }

    // Evaluate a student submission
    public AssignmentSubmission evaluateSubmission(
            Long submissionId,
            Long teacherId,
            Double marksObtained,
            String feedback) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher.");
        }

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Submission not found."));

        Long assignmentId = submission.getAssignment().getId();

        assignmentRepository.findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "You are not authorized to evaluate this submission."));

        if (marksObtained == null
                || marksObtained < 0
                || marksObtained > submission.getAssignment().getTotalMarks()) {

            throw new IllegalArgumentException(
                    "Marks must be between 0 and total marks.");
        }

        submission.setMarksObtained(marksObtained);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionStatus.EVALUATED);

        return submissionRepository.save(submission);
    }
}
