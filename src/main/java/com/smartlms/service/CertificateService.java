
package com.smartlms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Certificate;
import com.smartlms.entity.Course;
import com.smartlms.repository.CertificateRepository;
import com.smartlms.repository.UserRepository;
import com.smartlms.repository.CourseRepository;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CourseRepository courseRepository) {

        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Certificate generateCertificate(Long studentId, Long courseId) {

        if (certificateRepository.existsByStudentIdAndCourseId(
                studentId, courseId)) {

            return certificateRepository
                    .findByStudentIdAndCourseId(studentId, courseId)
                    .orElseThrow();
        }

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found"));

        Certificate certificate = new Certificate();

        certificate.setStudent(student);
        certificate.setCourse(course);

        certificate.setCertificateNumber(
                "SMARTLMS-" + UUID.randomUUID()
                        .toString().substring(0, 8).toUpperCase());

        certificate.setIssuedAt(LocalDateTime.now());

        return certificateRepository.save(certificate);
    }

    @Transactional(readOnly = true)
    public List<Certificate> getStudentCertificates(Long studentId) {

        return certificateRepository.findByStudentId(studentId);
    }
}
