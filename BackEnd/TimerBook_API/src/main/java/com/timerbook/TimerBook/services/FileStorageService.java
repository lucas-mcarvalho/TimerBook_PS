package com.timerbook.TimerBook.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    public FileStorageService() {
        try {
            this.fileStorageLocation = Paths.get("/uploads").toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("Upload directory: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subfolder) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(subfolder);
            Files.createDirectories(targetLocation);
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
            Path finalFilePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), finalFilePath, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/" + subfolder + "/" + uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível salvar o arquivo. Tente novamente!", ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.trim().isEmpty()) {
                Path fileToDeletePath = this.fileStorageLocation.resolve(
                        filePath.replace("uploads/", "")
                );
                Files.deleteIfExists(fileToDeletePath);
                System.out.println("Arquivo deletado: " + fileToDeletePath);
            }
        } catch (IOException ex) {
            System.err.println("Aviso: Não foi possível deletar o arquivo físico: " + filePath);
        }
    }
}