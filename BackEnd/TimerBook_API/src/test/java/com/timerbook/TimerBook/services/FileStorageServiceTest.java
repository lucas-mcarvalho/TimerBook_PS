package com.timerbook.TimerBook.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FileStorageServiceTest {

    @TempDir
    private Path tempDir;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = mock(FileStorageService.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ReflectionTestUtils.setField(service, "fileStorageLocation", tempDir);
    }

    @Test
    void storeFileShouldCreateSubfolderCopyFileAndReturnPublicPath() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "book.pdf", "application/pdf", "pdf-content".getBytes());

        String storedPath = service.storeFile(file, "pdfs");

        assertTrue(storedPath.startsWith("uploads/pdfs/"));
        assertTrue(storedPath.endsWith("_book.pdf"));
        Path storedFile = tempDir.resolve(storedPath.replace("uploads/", ""));
        assertTrue(Files.exists(storedFile));
        assertEquals("pdf-content", Files.readString(storedFile));
    }

    @Test
    void storeFileShouldWrapIOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("broken.pdf");
        when(file.getInputStream()).thenThrow(new IOException("disk error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.storeFile(file, "pdfs"));

        assertEquals("Não foi possível salvar o arquivo. Tente novamente!", exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void deleteFileShouldRemoveExistingFile() throws Exception {
        Path subfolder = tempDir.resolve("covers");
        Files.createDirectories(subfolder);
        Path file = subfolder.resolve("cover.png");
        Files.writeString(file, "img");

        service.deleteFile("uploads/covers/cover.png");

        assertFalse(Files.exists(file));
    }

    @Test
    void deleteFileShouldIgnoreNullOrBlankPath() {
        assertDoesNotThrow(() -> service.deleteFile(null));
        assertDoesNotThrow(() -> service.deleteFile(" "));
    }

    @Test
    void deleteFileShouldNotThrowWhenFileDoesNotExist() {
        assertDoesNotThrow(() -> service.deleteFile("uploads/covers/missing.png"));
    }
}
