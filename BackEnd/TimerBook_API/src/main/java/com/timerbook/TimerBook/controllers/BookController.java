package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.services.BookService;
import com.timerbook.TimerBook.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping(value = "/book")
@Tag(name = "Book", description = "Endpoints para gerenciamento de livros")
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private FileStorageService fileStorageService;

    public BookController() {}

    @Operation(summary = "Cria um novo livro", description = "Cria um livro com capa e PDF opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao salvar o livro")
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @Parameter(description = "Dados do livro em JSON") @RequestPart("book") Book book,
            @Parameter(description = "Imagem de capa (opcional)") @RequestPart(value = "cover", required = false) MultipartFile coverFile,
            @Parameter(description = "Arquivo PDF (opcional)") @RequestPart(value = "pdf", required = false) MultipartFile pdfFile) {
        try {
            if (coverFile != null && !coverFile.isEmpty()) {
                String coverPath = fileStorageService.storeFile(coverFile, "covers");
                book.setCoverUrl(coverPath);
            }
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String pdfPath = fileStorageService.storeFile(pdfFile, "pdfs");
                book.setDataPath(pdfPath);
            }
            bookService.create(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar o livro: " + e.getMessage());
        }
    }

    @Operation(summary = "Atualiza um livro existente", description = "Atualiza os dados de um livro pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao atualizar o livro")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @Parameter(description = "ID do livro") @PathVariable Long id,
            @Parameter(description = "Dados atualizados do livro") @RequestPart("book") Book bookDetails,
            @Parameter(description = "Nova imagem de capa (opcional)") @RequestPart(value = "cover", required = false) MultipartFile coverFile,
            @Parameter(description = "Novo arquivo PDF (opcional)") @RequestPart(value = "pdf", required = false) MultipartFile pdfFile) {
        try {
            Book existingBook = bookService.findById(id);
            if (coverFile != null && !coverFile.isEmpty()) {
                String coverPath = fileStorageService.storeFile(coverFile, "covers");
                bookDetails.setCoverUrl(coverPath);
            } else {
                bookDetails.setCoverUrl(existingBook.getCoverUrl());
            }
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String pdfPath = fileStorageService.storeFile(pdfFile, "pdfs");
                bookDetails.setDataPath(pdfPath);
            } else {
                bookDetails.setDataPath(existingBook.getDataPath());
            }
            bookService.update(id, bookDetails);
            return ResponseEntity.ok().body("Livro atualizado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar o livro: " + e.getMessage());
        }
    }

    @Operation(summary = "Lista todos os livros", description = "Retorna uma lista com todos os livros cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        List<Book> book = bookService.findALl();
        return ResponseEntity.ok().body(book);
    }

    @Operation(summary = "Remove um livro", description = "Deleta um livro e seus arquivos pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do livro a ser deletado") @PathVariable Long id) {
        Book book = bookService.findById(id);
        fileStorageService.deleteFile(book.getCoverUrl());
        fileStorageService.deleteFile(book.getDataPath());
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}