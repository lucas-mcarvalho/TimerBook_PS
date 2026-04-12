package com.timerbook.TimerBook.controllers;


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
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Criar livro",
            description = "Cria um livro com capa (imagem) e PDF opcionais"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> create(
            @Parameter(description = "ID do usuário dono do livro", example = "1")
            @RequestParam Long userId,

            @Parameter(description = "Nome do livro", example = "Harry Potter")
            @RequestPart("name") String name,

            @Parameter(description = "Descrição do livro", example = "Livro de fantasia")
            @RequestPart("description") String description,

            @Parameter(description = "Imagem de capa (arquivo)")
            @RequestPart(value = "cover", required = false) MultipartFile cover,

            @Parameter(description = "Arquivo PDF do livro")
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {

        BookDTO dto = new BookDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCover(cover);
        dto.setData(pdf);

        Book book = bookService.create(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @Operation(summary = "Listar livros", description = "Retorna todos os livros")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @Operation(summary = "Buscar livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(bookService.findById(id));
    }

    @Operation(summary = "Atualizar livro")
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

    @Operation(summary = "Deletar livro")
    @ApiResponse(responseCode = "204", description = "Livro deletado com sucesso")
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