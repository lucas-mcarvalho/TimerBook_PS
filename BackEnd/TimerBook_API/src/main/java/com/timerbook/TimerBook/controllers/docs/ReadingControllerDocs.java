package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Reading;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Readings", description = "Gerencia o ciclo de vida completo de uma leitura: iniciar, registrar progresso e finalizar")
public interface ReadingControllerDocs {

    @Operation(summary = "Inicia uma nova leitura", description = "Cria uma nova leitura para um livro específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leitura e sessão criadas com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reading.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<Reading> startReading(
            @Parameter(name = "id", description = "ID do usuário que está iniciando a leitura", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(name = "body", description = "Dados para iniciar a leitura.", required = true, schema = @Schema(implementation = InitReadingDTO.class))
            @RequestBody InitReadingDTO dto);

    @Operation(summary = "Finaliza uma leitura", description = "Marca uma leitura como concluída.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitura finalizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reading.class))),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<Reading> finishReading(
            @Parameter(name = "userId", description = "ID do usuário dono da leitura", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(name = "readingId", description = "ID da leitura a ser finalizada", required = true, example = "1")
            @PathVariable Long readingId,
            @Parameter(name = "body", description = "Dados para finalizar", required = true, schema = @Schema(implementation = FinishReadingDTO.class))
            @RequestBody FinishReadingDTO dto);

    @Operation(summary = "Obtém os dados de uma leitura")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitura retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reading.class))),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada", content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<Reading> getReading(
            @Parameter(name = "readingId", description = "ID da leitura a ser obtida", required = true, example = "1")
            @PathVariable Long readingId);

    @Operation(summary = "Busca todas as leituras de um livro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de leituras retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Reading.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado", content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<List<Reading>> getReadingsByBookId(
            @Parameter(name = "userId", description = "ID do usuário que possui as leituras", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(name = "bookId", description = "ID do livro para buscar suas leituras", required = true, example = "1")
            @PathVariable Long bookId);

    @Operation(summary = "Lista todas as leituras")
    @ApiResponse(responseCode = "200", description = "Lista de leituras retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Reading.class)))
    ResponseEntity<List<Reading>> getAllReadings();
}