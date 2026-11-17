
package com.smartlms.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.*;
import com.smartlms.repository.*;
import com.smartlms.service.StudentExamService;

@Controller
@RequestMapping("/student")
public class StudentExamController {

    private final StudentExamService studentExamService;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository attemptRepository;
    private final QuestionOptionRepository optionRepository;

    public StudentExamController(
            StudentExamService studentExamService,
            UserRepository userRepository,
            ExamRepository examRepository,
            ExamAttemptRepository attemptRepository,
            QuestionOptionRepository optionRepository) {

        this.studentExamService = studentExamService;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.attemptRepository = attemptRepository;
        this.optionRepository = optionRepository;
    }

    private AppUser getStudent(Principal principal) {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT || !student.isEnabled()) {
            throw new RuntimeException("Access denied");
        }

        return student;
    }

    // View Exam Details
    @GetMapping("/exam/{examId}")
    public String examDetails(
            @PathVariable Long examId,
            Principal principal,
            Model model) {

        AppUser student = getStudent(principal);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found"));

        if (!Boolean.TRUE.equals(exam.getActive())) {
            throw new RuntimeException("Exam is not published");
        }

        List<ExamAttempt> attempts =
                attemptRepository
                        .findByStudentIdAndExamIdOrderByAttemptNumberDesc(
                                student.getId(), examId);

        boolean passed = attempts.stream()
                .anyMatch(a ->
                        a.getResultStatus() == ExamResultStatus.PASSED);

        boolean activeAttempt = attempts.stream()
                .anyMatch(a ->
                        a.getResultStatus() == ExamResultStatus.IN_PROGRESS);

        int attemptsUsed = attempts.size();

        int remainingAttempts = Math.max(
                0, exam.getMaxAttempts() - attemptsUsed);

        model.addAttribute("exam", exam);
        model.addAttribute("attempts", attempts);
        model.addAttribute("attemptsUsed", attemptsUsed);
        model.addAttribute("remainingAttempts", remainingAttempts);
        model.addAttribute("passed", passed);
        model.addAttribute("activeAttempt", activeAttempt);

        return "student/exam-details";
    }

    // Start Examination
    @PostMapping("/exam/{examId}/start")
    public String startExam(
            @PathVariable Long examId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser student = getStudent(principal);

            ExamAttempt attempt =
                    studentExamService.startExam(
                            student.getId(), examId);

            return "redirect:/student/exam/attempt/" + attempt.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/student/exam/" + examId;
        }
    }

    // Open Examination Questions
    @GetMapping("/exam/attempt/{attemptId}")
    public String attemptExam(
            @PathVariable Long attemptId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser student = getStudent(principal);

            ExamAttempt attempt = attemptRepository
                    .findByIdAndStudentId(attemptId, student.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Attempt not found"));

            List<Question> questions =
                    studentExamService.getStudentExamQuestions(
                            attemptId, student.getId());

            Map<Long, List<QuestionOption>> questionOptions =
                    new HashMap<>();

            for (Question question : questions) {
                questionOptions.put(
                        question.getId(),
                        optionRepository.findByQuestionId(question.getId())
                );
            }

            long remainingSeconds = Math.max(
                    0,
                    Duration.between(
                            LocalDateTime.now(),
                            attempt.getExpiresAt()
                    ).getSeconds()
            );

            model.addAttribute("attempt", attempt);
            model.addAttribute("exam", attempt.getExam());
            model.addAttribute("questions", questions);
            model.addAttribute("questionOptions", questionOptions);
            model.addAttribute("remainingSeconds", remainingSeconds);

            return "student/exam-attempt";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/student/dashboard";
        }
    }

    // Submit Examination
    @PostMapping("/exam/attempt/{attemptId}/submit")
    public String submitExam(
            @PathVariable Long attemptId,
            Principal principal,
            @RequestParam MultiValueMap<String, String> formData,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser student = getStudent(principal);

            Map<Long, Long> selectedAnswers = new HashMap<>();

            for (String key : formData.keySet()) {

                if (key.startsWith("answers[") && key.endsWith("]")) {

                    String questionId = key.substring(
                            8, key.length() - 1);

                    String optionId = formData.getFirst(key);

                    if (optionId != null && !optionId.isBlank()) {
                        selectedAnswers.put(
                                Long.parseLong(questionId),
                                Long.parseLong(optionId)
                        );
                    }
                }
            }

            ExamAttempt result =
                    studentExamService.submitExam(
                            attemptId,
                            student.getId(),
                            selectedAnswers
                    );

            return "redirect:/student/exam/result/" + result.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/student/dashboard";
        }
    }

    // View Exam Result
    @GetMapping("/exam/result/{attemptId}")
    public String examResult(
            @PathVariable Long attemptId,
            Principal principal,
            Model model) {

        AppUser student = getStudent(principal);

        ExamAttempt attempt = attemptRepository
                .findByIdAndStudentId(attemptId, student.getId())
                .orElseThrow(() ->
                        new RuntimeException("Result not found"));

        model.addAttribute("attempt", attempt);
        model.addAttribute("exam", attempt.getExam());

        return "student/exam-result";
    }

    // Student Exam History
    @GetMapping("/exam-history")
    public String examHistory(
            Principal principal,
            Model model) {

        AppUser student = getStudent(principal);

        List<ExamAttempt> attempts =
                attemptRepository.findByStudentIdOrderByStartedAtDesc(
                        student.getId());

        long totalAttempts = attempts.size();

        long passedExams = attempts.stream()
                .filter(a ->
                        a.getResultStatus() == ExamResultStatus.PASSED)
                .count();

        long failedExams = attempts.stream()
                .filter(a ->
                        a.getResultStatus() == ExamResultStatus.FAILED)
                .count();

        long inProgressExams = attempts.stream()
                .filter(a ->
                        a.getResultStatus() == ExamResultStatus.IN_PROGRESS)
                .count();

        model.addAttribute("attempts", attempts);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("passedExams", passedExams);
        model.addAttribute("failedExams", failedExams);
        model.addAttribute("inProgressExams", inProgressExams);

        return "student/exam-history";
    }
}
