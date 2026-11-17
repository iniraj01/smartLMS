
package com.smartlms.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartlms.entity.Certificate;
import com.smartlms.repository.CertificateRepository;

@Controller
public class CertificateVerificationController {

    private final CertificateRepository certificateRepository;

    public CertificateVerificationController(
            CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/verify-certificate")
    public String verificationPage() {
        return "verify-certificate";
    }

    @PostMapping("/verify-certificate")
    public String verifyCertificate(
            @RequestParam String certificateNumber,
            Model model) {

        String number = certificateNumber.trim();

        if (number.isEmpty()) {
            model.addAttribute("error",
                    "Please enter a certificate number.");
            return "verify-certificate";
        }

        Optional<Certificate> result =
                certificateRepository.findByCertificateNumber(number);

        if (result.isPresent()) {
            Certificate certificate = result.get();

            model.addAttribute("certificate", certificate);
            model.addAttribute("verified", true);

        } else {
            model.addAttribute("verified", false);
            model.addAttribute("error",
                    "Certificate not found. Please check the certificate number.");
        }

        return "verify-certificate";
    }
}
