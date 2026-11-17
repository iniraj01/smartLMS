
package com.smartlms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.*;
import com.smartlms.repository.*;

@Service
public class StudentExamService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository attemptRepository;
    private final ExamAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final LessonProgressService lessonProgressService;
    private final CertificateService certificateService;

    public StudentExamService(
            ExamRepository examRepository,
            ExamAttemptRepository attemptRepository,
            ExamAnswerRepository answerRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            LessonProgressService lessonProgressService,
            CertificateService certificateService) {

        this.examRepository = examRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.lessonProgressService = lessonProgressService;
        this.certificateService = certificateService;
    }

    // Validate Student
    private AppUser getStudent(Long studentId) {

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT || !student.isEnabled()) {
            throw new RuntimeException("Invalid student account");
        }

        return student;
    }

    // Get Published Exam
    private Exam getPublishedExam(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found"));

        if (!Boolean.TRUE.equals(exam.getActive())) {
            throw new RuntimeException("This examination is not published");
        }

        return exam;
    }

    // Check Student Eligibility
    private void validateEligibility(Long studentId, Exam exam) {

        boolean enrolled = enrollmentService
                .getActiveEnrollments(studentId)
                .stream()
                .anyMatch(e ->
                        e.getCourse().getId()
                                .equals(exam.getCourse().getId()));

        if (!enrolled) {
            throw new RuntimeException(
                    "You are not enrolled in this course");
        }

        // Check all lessons completed
        int progressPercentage =
                lessonProgressService.getCourseProgressPercentage(
                        studentId,
                        exam.getCourse().getId()
                );

        if (progressPercentage < 100) {
            throw new RuntimeException(
                    "Complete all course lessons before attempting the exam");
        }

        // Check whether student already passed
        boolean alreadyPassed = attemptRepository
                .existsByStudentIdAndExamIdAndResultStatus(
                        studentId,
                        exam.getId(),
                        ExamResultStatus.PASSED
                );

        if (alreadyPassed) {
            throw new RuntimeException(
                    "You have already passed this examination");
        }
    }

    // Start Examination
    @Transactional
    public ExamAttempt startExam(Long studentId, Long examId) {

        AppUser student = getStudent(studentId);

        Exam exam = getPublishedExam(examId);

        validateEligibility(studentId, exam);

        List<ExamAttempt> previousAttempts =
                attemptRepository
                        .findByStudentIdAndExamIdOrderByAttemptNumberDesc(
                                studentId, examId);

        // Check existing active attempt
        boolean activeAttempt = previousAttempts.stream()
                .anyMatch(a ->
                        a.getResultStatus() == ExamResultStatus.IN_PROGRESS);

        if (activeAttempt) {
            throw new RuntimeException(
                    "You already have an active examination");
        }

        // Maximum attempts validation
        int attemptNumber = previousAttempts.size() + 1;

        if (attemptNumber > exam.getMaxAttempts()) {
            throw new RuntimeException(
                    "Maximum exam attempts reached");
        }

        // Check questions
        if (questionRepository.countByExamId(examId) == 0) {
            throw new RuntimeException(
                    "No questions available in this examination");
        }

        ExamAttempt attempt = new ExamAttempt();

        attempt.setStudent(student);
        attempt.setExam(exam);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStartedAt(LocalDateTime.now());

        attempt.setExpiresAt(
                LocalDateTime.now().plusMinutes(
                        exam.getDurationMinutes()));

        attempt.setResultStatus(ExamResultStatus.IN_PROGRESS);

        return attemptRepository.save(attempt);
    }

    // Get Questions for Student
    @Transactional(readOnly = true)
    public List<Question> getStudentExamQuestions(
            Long attemptId, Long studentId) {

        ExamAttempt attempt = attemptRepository
                .findByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() ->
                        new RuntimeException("Exam attempt not found"));

        if (attempt.getResultStatus()
                != ExamResultStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "This examination is no longer active");
        }

        if (LocalDateTime.now().isAfter(attempt.getExpiresAt())) {
            throw new RuntimeException(
                    "Examination time has expired");
        }

        return questionRepository
                .findByExamIdOrderByQuestionOrderAsc(
                        attempt.getExam().getId());
    }

    // Submit and Evaluate Examination
    @Transactional
    public ExamAttempt submitExam(
            Long attemptId,
            Long studentId,
            Map<Long, Long> selectedAnswers) {

        ExamAttempt attempt = attemptRepository
                .findByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() ->
                        new RuntimeException("Exam attempt not found"));

        if (attempt.getResultStatus()
                != ExamResultStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Examination has already been submitted");
        }

        // Check Exam Expiry
        if (LocalDateTime.now().isAfter(attempt.getExpiresAt())) {

            attempt.setResultStatus(ExamResultStatus.TIME_EXPIRED);
            attempt.setSubmittedAt(LocalDateTime.now());

            // Save and commit expired attempt
            return attemptRepository.save(attempt);
        }

        double obtainedMarks = 0;

        List<Question> questions =
                questionRepository
                        .findByExamIdOrderByQuestionOrderAsc(
                                attempt.getExam().getId());

        for (Question question : questions) {

            Long selectedOptionId = selectedAnswers == null
                    ? null
                    : selectedAnswers.get(question.getId());

            ExamAnswer answer = new ExamAnswer();

            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setCorrect(false);
            answer.setMarksAwarded(0.0);

            if (selectedOptionId != null) {

                QuestionOption selectedOption = optionRepository
                        .findById(selectedOptionId)
                        .orElseThrow(() ->
                                new RuntimeException("Invalid option selected"));

                // Verify option belongs to this question
                if (!selectedOption.getQuestion().getId()
                        .equals(question.getId())) {

                    throw new RuntimeException(
                            "Selected option does not belong to this question");
                }

                answer.setSelectedOption(selectedOption);

                // Automatic answer evaluation
                if (Boolean.TRUE.equals(selectedOption.getCorrect())) {

                    answer.setCorrect(true);

                    answer.setMarksAwarded(question.getMarks());

                    obtainedMarks += question.getMarks();
                }
            }

            answerRepository.save(answer);
        }

        // Calculate percentage
        double percentage =
                (obtainedMarks / attempt.getExam().getTotalMarks()) * 100.0;

        // Check passing percentage
        boolean passed =
                percentage >= attempt.getExam().getPassingPercentage();

        attempt.setMarksObtained(obtainedMarks);
        attempt.setPercentage(percentage);

        attempt.setResultStatus(
                passed
                        ? ExamResultStatus.PASSED
                        : ExamResultStatus.FAILED
        );

        attempt.setSubmittedAt(LocalDateTime.now());

        ExamAttempt savedAttempt = attemptRepository.save(attempt);

        // Generate certificate automatically after passing
        if (passed) {

            certificateService.generateCertificate(
                    studentId,
                    attempt.getExam().getCourse().getId()
            );
        }

        return savedAttempt;
    }

    // Get Student Exam Results
    @Transactional(readOnly = true)
    public List<ExamAttempt> getStudentResults(Long studentId) {

        getStudent(studentId);

        return attemptRepository.findAll()
                .stream()
                .filter(a ->
                        a.getStudent().getId().equals(studentId))
                .toList();
    }
}
