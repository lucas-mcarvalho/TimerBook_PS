package com.timerbook.TimerBook.controllers;


import com.timerbook.TimerBook.controllers.docs.BookcontrollerDocs;
import com.timerbook.TimerBook.dto.BookDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/book")
@Tag(name = "Book", description = "Api de livros")
public class BookController implements BookcontrollerDocs {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> create(
            @RequestParam Long userId,
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        BookDTO dto = new BookDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCover(cover);
        dto.setData(pdf);

        Book book = bookService.create(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(bookService.findById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> update(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id,

            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {

        BookDTO dto = new BookDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCover(cover);
        dto.setData(pdf);

        return ResponseEntity.ok(bookService.update(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id) {

        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/user/{userId}")
    public List<Book> getBooksByUser(@PathVariable Long userId) {
        return bookService.findByUserId(userId);
    }
}