
package com.smartlms.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Certificate;
import com.smartlms.repository.CertificateRepository;
import com.smartlms.repository.UserRepository;

import java.security.Principal;

@Controller
@RequestMapping("/student/certificates")
public class CertificateDownloadController {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    public CertificateDownloadController(
            CertificateRepository certificateRepository,
            UserRepository userRepository) {

        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/download")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long id,
            Principal principal) throws IOException {

        AppUser student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Certificate not found"));

        // Student can download only their own certificate
        if (!certificate.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException(
                    "You cannot download another student's certificate");
        }

        byte[] pdf = generatePDF(certificate);

        String filename = "SmartLMS-Certificate-"
                + certificate.getCertificateNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] generatePDF(Certificate certificate) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

        	PDPage page = new PDPage(
        	        new PDRectangle(842, 595)
        	);
            document.addPage(page);

            PDType1Font regular =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDType1Font bold =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float pageWidth = page.getMediaBox().getWidth();

                // Outer border
                content.setLineWidth(3);
                content.addRect(25, 25,
                        pageWidth - 50,
                        page.getMediaBox().getHeight() - 50);
                content.stroke();

                // Inner border
                content.setLineWidth(1);
                content.addRect(35, 35,
                        pageWidth - 70,
                        page.getMediaBox().getHeight() - 70);
                content.stroke();

                // Certificate heading
                drawCenteredText(content, bold,
                        "SMARTLMS", 30, 410, pageWidth);

                drawCenteredText(content, bold,
                        "CERTIFICATE OF COMPLETION",
                        24, 355, pageWidth);

                drawCenteredText(content, regular,
                        "This certificate is proudly presented to",
                        16, 300, pageWidth);

                // Student name
                drawCenteredText(content, bold,
                        certificate.getStudent().getName(),
                        30, 245, pageWidth);

                drawCenteredText(content, regular,
                        "For successfully completing the course",
                        16, 195, pageWidth);

                // Course name
                drawCenteredText(content, bold,
                        certificate.getCourse().getTitle(),
                        24, 150, pageWidth);

                String issueDate = certificate.getIssuedAt()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

                drawCenteredText(content, regular,
                        "Issue Date: " + issueDate,
                        13, 95, pageWidth);

                drawCenteredText(content, regular,
                        "Certificate ID: "
                                + certificate.getCertificateNumber(),
                        12, 65, pageWidth);

                // Signature section
                content.beginText();
                content.setFont(bold, 13);
                content.newLineAtOffset(pageWidth - 190, 95);
                content.showText("SmartLMS Authority");
                content.endText();
            }

            document.save(output);

            return output.toByteArray();
        }
    }

    private void drawCenteredText(
            PDPageContentStream content,
            PDType1Font font,
            String text,
            int fontSize,
            float y,
            float pageWidth) throws IOException {

        float textWidth = font.getStringWidth(text) / 1000 * fontSize;

        float x = (pageWidth - textWidth) / 2;

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }
}
