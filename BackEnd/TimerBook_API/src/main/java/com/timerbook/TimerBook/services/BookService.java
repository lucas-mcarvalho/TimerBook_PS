package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.BookDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.exception.BookException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository,
                       UserRepository userRepository,
                       FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookException("Livro não encontrado"));
    }

    public Book create(Long userId, BookDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String coverPath = null;
        if (dto.getCover() != null && !dto.getCover().isEmpty()) {
            coverPath = fileStorageService.storeFile(dto.getCover(), "covers");
        }

        String pdfPath = null;
        if (dto.getData() != null && !dto.getData().isEmpty()) {
            pdfPath = fileStorageService.storeFile(dto.getData(), "pdfs");
        }

        Book book = new Book();
        book.setName(dto.getName());
        book.setDescription(dto.getDescription());
        book.setCoverUrl(coverPath);
        book.setDataPath(pdfPath);
        book.setUser(user);

        return bookRepository.save(book);
    }

    public Book update(Long id, BookDTO dto) {

        Book book = findById(id);

        book.setName(dto.getName());
        book.setDescription(dto.getDescription());

        if (dto.getCover() != null && !dto.getCover().isEmpty()) {
            fileStorageService.deleteFile(book.getCoverUrl());
            String newCover = fileStorageService.storeFile(dto.getCover(), "covers");
            book.setCoverUrl(newCover);
        }

        if (dto.getData() != null && !dto.getData().isEmpty()) {
            fileStorageService.deleteFile(book.getDataPath());
            String newPdf = fileStorageService.storeFile(dto.getData(), "pdfs");
            book.setDataPath(newPdf);
        }

        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = findById(id);

        fileStorageService.deleteFile(book.getCoverUrl());
        fileStorageService.deleteFile(book.getDataPath());

        bookRepository.delete(book);
    }
}