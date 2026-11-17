
package com.smartlms.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadDirectory;

    public FileStorageService(
            @Value("${app.upload.dir:uploads/assignments}")
            String uploadDirectory) {

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    // Save PDF file
    public String storePdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a PDF file.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "PDF file size must not exceed 5 MB.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null ||
                !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        try (InputStream inputStream = file.getInputStream()) {

            byte[] signature = inputStream.readNBytes(5);

            if (!new String(signature,
                    java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new IllegalArgumentException("Invalid PDF file.");
            }

            Files.createDirectories(uploadDirectory);

            String storedFilename =
                    UUID.randomUUID().toString() + ".pdf";

            Path targetPath = uploadDirectory.resolve(storedFilename)
                    .normalize();

            if (!targetPath.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("Invalid file path.");
            }

            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);

            return storedFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Unable to save PDF file.", ex);
        }
    }

    // Get stored PDF path
    public Path getFilePath(String filename) {

        Path filePath = uploadDirectory.resolve(filename)
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        return filePath;
    }

    // Delete stored PDF
    public void deleteFile(String filename) {

        if (filename == null || filename.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(getFilePath(filename));
        } catch (IOException ex) {
            throw new RuntimeException("Unable to delete PDF file.", ex);
        }
    }
}
