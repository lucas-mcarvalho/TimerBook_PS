package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.BookCreationResponseDTO;
import com.timerbook.TimerBook.models.Book;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Books", description = "Endpoints for Managing Book")
public interface BookcontrollerDocs {

    @Operation(
            summary = "Criar livro",
            description = "Cria um livro com capa (imagem) e PDF opcionais"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    ResponseEntity<BookCreationResponseDTO> create(@Parameter(description = "ID do usuário dono do livro", example = "1") Long userId,
                                                   @Parameter(description = "Nome do livro", example = "Harry Potter") String name,
                                                   @Parameter(description = "Descrição do livro", example = "Livro de fantasia") String description,
                                                   @Parameter(description = "Imagem de capa (arquivo)") MultipartFile cover,
                                                   @Parameter(description = "Arquivo PDF do livro") MultipartFile pdf
    );

    @Operation(summary = "Listar livros", description = "Retorna todos os livros")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    ResponseEntity<List<Book>> getAll();

    @Operation(summary = "Atualizar livro")
    @Parameter(description = "ID do livro", example = "1")
    ResponseEntity<Book> update(  @PathVariable Long id,
                                  @RequestPart("name") String name,
                                  @RequestPart("description") String description,
                                  @RequestPart(value = "cover", required = false) MultipartFile cover,
                                  @RequestPart(value = "pdf", required = false) MultipartFile pdf);


    @Operation(summary = "Buscar livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    ResponseEntity<Book> getById(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id);

    @Operation(summary = "Deletar livro")
    @ApiResponse(responseCode = "204", description = "Livro deletado com sucesso")
    ResponseEntity<Void> delete(
            @Parameter(description = "ID do livro", example = "1")
            @PathVariable Long id);

    @Operation(summary = "Listar livros de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<List<Book>> getBooksByUser(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId);
}
