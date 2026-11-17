
package com.smartlms.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Exam;
import com.smartlms.entity.ExamAttempt;
import com.smartlms.entity.ExamResultStatus;
import com.smartlms.entity.Question;
import com.smartlms.entity.QuestionOption;
import com.smartlms.entity.Role;

import com.smartlms.repository.UserRepository;
import com.smartlms.repository.ExamRepository;
import com.smartlms.repository.ExamAttemptRepository;
import com.smartlms.repository.QuestionOptionRepository;

import com.smartlms.service.ExamService;
import com.smartlms.service.TeacherCourseService;

@Controller
@RequestMapping("/teacher/exams")
public class TeacherExamController {

    private final ExamService examService;
    private final TeacherCourseService teacherCourseService;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionOptionRepository optionRepository;
    private final ExamAttemptRepository attemptRepository;

    public TeacherExamController(
            ExamService examService,
            TeacherCourseService teacherCourseService,
            UserRepository userRepository,
            ExamRepository examRepository,
            QuestionOptionRepository optionRepository,
            ExamAttemptRepository attemptRepository) {

        this.examService = examService;
        this.teacherCourseService = teacherCourseService;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.optionRepository = optionRepository;
        this.attemptRepository = attemptRepository;
    }

    // Get Logged-in Teacher
    private AppUser getTeacher(Principal principal) {

        AppUser teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found"));

        if (teacher.getRole() != Role.TEACHER || !teacher.isEnabled()) {
            throw new RuntimeException("Access denied");
        }

        return teacher;
    }

    // Teacher Exam Dashboard
    @GetMapping
    public String examDashboard(
            Principal principal,
            Model model) {

        AppUser teacher = getTeacher(principal);

        model.addAttribute("exams",
                examService.getTeacherExams(teacher.getId()));

        model.addAttribute("courses",
                teacherCourseService.getTeacherCourses(teacher.getId()));

        return "teacher/exams";
    }

    // Create Exam
    @PostMapping("/create")
    public String createExam(
            Principal principal,
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "100") Double totalMarks,
            @RequestParam(defaultValue = "40") Double passingPercentage,
            @RequestParam(defaultValue = "30") Integer durationMinutes,
            @RequestParam(defaultValue = "3") Integer maxAttempts,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getTeacher(principal);

            examService.createExam(
                    courseId,
                    teacher.getId(),
                    title,
                    description,
                    totalMarks,
                    passingPercentage,
                    durationMinutes,
                    maxAttempts
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Exam created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/exams";
    }

    // View Questions and Options
    @GetMapping("/{examId}/questions")
    public String viewQuestions(
            @PathVariable Long examId,
            Principal principal,
            Model model) {

        AppUser teacher = getTeacher(principal);

        Exam exam = examRepository
                .findByIdAndTeacherId(examId, teacher.getId())
                .orElseThrow(() ->
                        new RuntimeException("Exam not found"));

        List<Question> questions =
                examService.getExamQuestions(examId, teacher.getId());

        Map<Long, List<QuestionOption>> questionOptions =
                new HashMap<>();

        for (Question question : questions) {
            questionOptions.put(
                    question.getId(),
                    optionRepository.findByQuestionId(question.getId())
            );
        }

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("questionOptions", questionOptions);

        return "teacher/exam-questions";
    }

    // Add MCQ Question
    @PostMapping("/{examId}/questions/add")
    public String addQuestion(
            @PathVariable Long examId,
            Principal principal,
            @RequestParam String questionText,
            @RequestParam Double marks,
            @RequestParam("options") List<String> options,
            @RequestParam Integer correctOptionIndex,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getTeacher(principal);

            examService.addQuestion(
                    examId,
                    teacher.getId(),
                    questionText,
                    marks,
                    options,
                    correctOptionIndex
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Question added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    // Publish Exam
    @PostMapping("/{examId}/publish")
    public String publishExam(
            @PathVariable Long examId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getTeacher(principal);

            examService.publishExam(examId, teacher.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Exam published successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    // Unpublish Exam
    @PostMapping("/{examId}/unpublish")
    public String unpublishExam(
            @PathVariable Long examId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getTeacher(principal);

            examService.unpublishExam(examId, teacher.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Exam unpublished successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/teacher/exams/" + examId + "/questions";
    }

    // Teacher Exam Performance Report
    @GetMapping("/{examId}/results")
    public String examResults(
            @PathVariable Long examId,
            Principal principal,
            Model model) {

        AppUser teacher = getTeacher(principal);

        // Verify that this exam belongs to the logged-in teacher
        Exam exam = examRepository
                .findByIdAndTeacherId(examId, teacher.getId())
                .orElseThrow(() ->
                        new RuntimeException("Exam not found"));

        List<ExamAttempt> attempts =
                attemptRepository.findByExamIdOrderByStartedAtDesc(examId);

        long totalAttempts = attempts.size();

        long passedCount = attempts.stream()
                .filter(a -> a.getResultStatus() == ExamResultStatus.PASSED)
                .count();

        long failedCount = attempts.stream()
                .filter(a -> a.getResultStatus() == ExamResultStatus.FAILED)
                .count();

        long inProgressCount = attempts.stream()
                .filter(a -> a.getResultStatus() == ExamResultStatus.IN_PROGRESS)
                .count();

        long expiredCount = attempts.stream()
                .filter(a -> a.getResultStatus() == ExamResultStatus.TIME_EXPIRED)
                .count();

        model.addAttribute("exam", exam);
        model.addAttribute("attempts", attempts);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("passedCount", passedCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("expiredCount", expiredCount);

        return "teacher/exam-results";
    }
}
