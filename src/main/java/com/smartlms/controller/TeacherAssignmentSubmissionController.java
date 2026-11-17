
package com.smartlms.controller;

import java.io.IOException;
import java.security.Principal;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Role;
import com.smartlms.entity.AssignmentSubmission;
import com.smartlms.entity.SubmissionStatus;
import com.smartlms.repository.UserRepository;
import com.smartlms.service.AssignmentSubmissionService;
import com.smartlms.service.FileStorageService;

@Controller
@RequestMapping("/teacher")
public class TeacherAssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public TeacherAssignmentSubmissionController(
            AssignmentSubmissionService submissionService,
            UserRepository userRepository,
            FileStorageService fileStorageService) {

        this.submissionService = submissionService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // View all submissions received by teacher
    @GetMapping("/submissions")
    public String viewSubmissions(Principal principal, Model model) {

        AppUser teacher = getTeacher(principal);

        List<AssignmentSubmission> submissions =
                submissionService.getTeacherSubmissions(teacher.getId());

        long total = submissions.size();

        long pending = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED)
                .count();

        long evaluated = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.EVALUATED)
                .count();

        model.addAttribute("submissions", submissions);
        model.addAttribute("teacherName", teacher.getName());
        model.addAttribute("totalSubmissions", total);
        model.addAttribute("pendingSubmissions", pending);
        model.addAttribute("evaluatedSubmissions", evaluated);

        return "teacher/submissions";
    }

    // Securely download student assignment PDF
    @GetMapping("/submissions/{id}/file")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @PathVariable Long id,
            Principal principal) throws IOException {

        AppUser teacher = getTeacher(principal);

        // Service will verify that this submission belongs
        // to an assignment assigned to this teacher.
        AssignmentSubmission submission =
                submissionService.getTeacherSubmission(id, teacher.getId());

        if (submission.getStoredFileName() == null
                || submission.getStoredFileName().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = fileStorageService.getFilePath(
                submission.getStoredFileName());

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String downloadName = submission.getOriginalFileName();

        if (downloadName == null || downloadName.isBlank()) {
            downloadName = "assignment.pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(downloadName)
                                .build()
                                .toString()
                )
                .body(resource);
    }

    // Evaluate student submission
    @PostMapping("/submissions/{id}/evaluate")
    public String evaluateSubmission(
            @PathVariable Long id,
            @RequestParam Double marksObtained,
            @RequestParam(required = false) String feedback,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser teacher = getTeacher(principal);

            submissionService.evaluateSubmission(
                    id,
                    teacher.getId(),
                    marksObtained,
                    feedback
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Submission evaluated successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/teacher/submissions";
    }

    // Validate logged-in teacher
    private AppUser getTeacher(Principal principal) {

        AppUser teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Access denied.");
        }

        return teacher;
    }
}
