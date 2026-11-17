
package com.smartlms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.*;
import com.smartlms.repository.*;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ExamService(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {

        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    private AppUser getTeacher(Long teacherId) {
        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only teachers can manage exams");
        }

        return teacher;
    }

    private Exam getOwnedExam(Long examId, Long teacherId) {

        return examRepository.findByIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new RuntimeException("Exam not found or access denied"));
    }

    @Transactional
    public Exam createExam(
            Long courseId,
            Long teacherId,
            String title,
            String description,
            Double totalMarks,
            Double passingPercentage,
            Integer durationMinutes,
            Integer maxAttempts) {

        AppUser teacher = getTeacher(teacherId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getTeacher() == null ||
                !course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("This course is not assigned to you");
        }

        if (examRepository.existsByCourseId(courseId)) {
            throw new RuntimeException("Final exam already exists for this course");
        }

        if (title == null || title.isBlank()) {
            throw new RuntimeException("Exam title is required");
        }

        if (totalMarks == null || totalMarks <= 0) {
            throw new RuntimeException("Total marks must be greater than zero");
        }

        if (passingPercentage == null ||
                passingPercentage <= 0 || passingPercentage > 100) {
            throw new RuntimeException("Invalid passing percentage");
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new RuntimeException("Invalid exam duration");
        }

        if (maxAttempts == null || maxAttempts < 1 || maxAttempts > 3) {
            throw new RuntimeException("Maximum attempts must be between 1 and 3");
        }

        Exam exam = new Exam();
        exam.setTitle(title.trim());
        exam.setDescription(description);
        exam.setTotalMarks(totalMarks);
        exam.setPassingPercentage(passingPercentage);
        exam.setDurationMinutes(durationMinutes);
        exam.setMaxAttempts(maxAttempts);
        exam.setCourse(course);
        exam.setTeacher(teacher);
        exam.setActive(false);

        return examRepository.save(exam);
    }

    @Transactional
    public Question addQuestion(
            Long examId,
            Long teacherId,
            String questionText,
            Double marks,
            List<String> options,
            Integer correctOptionIndex) {

        Exam exam = getOwnedExam(examId, teacherId);

        if (Boolean.TRUE.equals(exam.getActive())) {
            throw new RuntimeException("Unpublish the exam before editing questions");
        }

        if (questionText == null || questionText.isBlank()) {
            throw new RuntimeException("Question text is required");
        }

        if (marks == null || marks <= 0) {
            throw new RuntimeException("Question marks must be greater than zero");
        }

        if (options == null || options.size() != 4) {
            throw new RuntimeException("Exactly four options are required");
        }

        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new RuntimeException("All four options are required");
            }
        }

        if (correctOptionIndex == null ||
                correctOptionIndex < 0 || correctOptionIndex > 3) {
            throw new RuntimeException("Select the correct answer");
        }

        double existingMarks = questionRepository
                .findByExamIdOrderByQuestionOrderAsc(examId)
                .stream()
                .mapToDouble(Question::getMarks)
                .sum();

        if (existingMarks + marks > exam.getTotalMarks()) {
            throw new RuntimeException("Question marks exceed exam total marks");
        }

        Question question = new Question();
        question.setQuestionText(questionText.trim());
        question.setMarks(marks);
        question.setExam(exam);

        long questionCount = questionRepository.countByExamId(examId);
        question.setQuestionOrder((int) questionCount + 1);

        Question savedQuestion = questionRepository.save(question);

        for (int i = 0; i < 4; i++) {
            QuestionOption option = new QuestionOption();
            option.setOptionText(options.get(i).trim());
            option.setCorrect(i == correctOptionIndex);
            option.setQuestion(savedQuestion);

            optionRepository.save(option);
        }

        return savedQuestion;
    }

    @Transactional(readOnly = true)
    public List<Exam> getTeacherExams(Long teacherId) {
        getTeacher(teacherId);
        return examRepository.findAll().stream()
                .filter(exam -> exam.getTeacher().getId().equals(teacherId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Question> getExamQuestions(Long examId, Long teacherId) {
        getOwnedExam(examId, teacherId);
        return questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);
    }

    @Transactional
    public Exam publishExam(Long examId, Long teacherId) {

        Exam exam = getOwnedExam(examId, teacherId);

        List<Question> questions =
                questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        if (questions.isEmpty()) {
            throw new RuntimeException("Add questions before publishing the exam");
        }

        double questionMarks = 0;

        for (Question question : questions) {

            List<QuestionOption> options =
                    optionRepository.findByQuestionId(question.getId());

            if (options.size() != 4) {
                throw new RuntimeException("Every question must have four options");
            }

            long correctAnswers = options.stream()
                    .filter(option -> Boolean.TRUE.equals(option.getCorrect()))
                    .count();

            if (correctAnswers != 1) {
                throw new RuntimeException("Every question must have exactly one correct answer");
            }

            questionMarks += question.getMarks();
        }

        if (Math.abs(questionMarks - exam.getTotalMarks()) > 0.001) {
            throw new RuntimeException("Question marks must equal exam total marks");
        }

        exam.setActive(true);
        return examRepository.save(exam);
    }

    @Transactional
    public Exam unpublishExam(Long examId, Long teacherId) {

        Exam exam = getOwnedExam(examId, teacherId);
        exam.setActive(false);

        return examRepository.save(exam);
    }
}
