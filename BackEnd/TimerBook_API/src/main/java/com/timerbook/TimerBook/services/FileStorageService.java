package com.timerbook.TimerBook.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.type:local}")
    private String storageType = "local";

    @Value("${app.aws.s3.bucket:}")
    private String bucketName;

    @Value("${app.aws.region:${AWS_REGION:us-east-1}}")
    private String awsRegion = "us-east-1";

    private Path fileStorageLocation;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (isS3Storage()) {
            logger.info("File storage ativo: S3 bucket={} region={}", bucketName, awsRegion);
            return;
        }

        ensureLocalStorageLocation();
        logger.info("File storage ativo: local directory={}", this.fileStorageLocation);
    }

    public String storeFile(MultipartFile file, String subfolder) {
        String storagePath = buildStoragePath(file.getOriginalFilename(), subfolder);

        if (isS3Storage()) {
            return storeFileInS3(file, storagePath);
        }

        return storeFileLocally(file, storagePath);
    }

    public byte[] loadFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new RuntimeException("Arquivo não informado.");
        }

        if (isS3Storage()) {
            return loadFileFromS3(filePath);
        }

        return loadFileLocally(filePath);
    }

    public String getContentType(String filePath) {
        String lowerPath = String.valueOf(filePath).toLowerCase(Locale.ROOT);

        if (lowerPath.endsWith(".pdf")) return "application/pdf";
        if (lowerPath.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lowerPath.endsWith(".webp")) return "image/webp";
        if (lowerPath.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.trim().isEmpty()) {
                if (isS3Storage()) {
                    deleteFileFromS3(filePath);
                } else {
                    deleteFileLocally(filePath);
                }
            }
        } catch (IOException | S3Exception ex) {
            System.err.println("Aviso: Não foi possível deletar o arquivo físico: " + filePath);
        }
    }

    private String storeFileLocally(MultipartFile file, String storagePath) {
        try {
            ensureLocalStorageLocation();
            Path finalFilePath = this.fileStorageLocation.resolve(storagePath.replace("uploads/", ""));
            Path targetLocation = finalFilePath.getParent();
            Files.createDirectories(targetLocation);
            Files.copy(file.getInputStream(), finalFilePath, StandardCopyOption.REPLACE_EXISTING);

            return storagePath;

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível salvar o arquivo. Tente novamente!", ex);
        }
    }

    private String storeFileInS3(MultipartFile file, String storagePath) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(storagePath)
                    .contentType(file.getContentType())
                    .build();

            getS3Client().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("Arquivo enviado ao S3: bucket={} key={}", getBucketName(), storagePath);
            return storagePath;
        } catch (IOException ex) {
            logger.error("Erro de leitura ao enviar arquivo para S3: key={}", storagePath, ex);
            throw new RuntimeException("Não foi possível salvar o arquivo. Tente novamente!", ex);
        } catch (S3Exception ex) {
            logger.error("Erro da AWS ao enviar arquivo para S3: bucket={} key={} status={} awsCode={} awsMessage={}",
                    getBucketName(),
                    storagePath,
                    ex.statusCode(),
                    ex.awsErrorDetails() != null ? ex.awsErrorDetails().errorCode() : "unknown",
                    ex.awsErrorDetails() != null ? ex.awsErrorDetails().errorMessage() : ex.getMessage(),
                    ex);
            throw new RuntimeException("Não foi possível salvar o arquivo. Tente novamente!", ex);
        }
    }

    private byte[] loadFileLocally(String filePath) {
        try {
            Path path = resolveLocalPath(filePath);
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível ler o arquivo.", ex);
        }
    }

    private byte[] loadFileFromS3(String filePath) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(normalizeStoragePath(filePath))
                    .build();

            return getS3Client().getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException ex) {
            throw new RuntimeException("Arquivo não encontrado.", ex);
        } catch (S3Exception ex) {
            throw new RuntimeException("Não foi possível ler o arquivo.", ex);
        }
    }

    private void deleteFileLocally(String filePath) throws IOException {
        Path fileToDeletePath = resolveLocalPath(filePath);
        Files.deleteIfExists(fileToDeletePath);
        logger.info("Arquivo local deletado: {}", fileToDeletePath);
    }

    private void deleteFileFromS3(String filePath) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(getBucketName())
                .key(normalizeStoragePath(filePath))
                .build();

        getS3Client().deleteObject(request);
    }

    private Path resolveLocalPath(String filePath) {
        ensureLocalStorageLocation();
        Path resolved = this.fileStorageLocation.resolve(
                normalizeStoragePath(filePath).replace("uploads/", "")
        ).normalize();

        if (!resolved.startsWith(this.fileStorageLocation)) {
            throw new RuntimeException("Caminho de arquivo inválido.");
        }

        return resolved;
    }

    private String buildStoragePath(String originalFileName, String subfolder) {
        String cleanSubfolder = String.valueOf(subfolder).replace("\\", "/").replaceAll("^/+|/+$", "");
        String safeFileName = Paths.get(String.valueOf(originalFileName)).getFileName().toString();
        return "uploads/" + cleanSubfolder + "/" + UUID.randomUUID() + "_" + safeFileName;
    }

    private String normalizeStoragePath(String filePath) {
        return filePath.trim().replace("\\", "/").replaceAll("^/+", "");
    }

    private boolean isS3Storage() {
        return "s3".equalsIgnoreCase(storageType);
    }

    private void ensureLocalStorageLocation() {
        if (this.fileStorageLocation != null) {
            return;
        }

        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    private String getBucketName() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new RuntimeException("Bucket S3 não configurado. Defina AWS_S3_BUCKET ou app.aws.s3.bucket.");
        }
        return bucketName;
    }

    private S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return s3Client;
    }
}
