package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.models.Reading;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Statistics",
        description = "Consulta estatísticas e métricas de leitura: páginas lidas, tempo gasto, sequências e muito mais"
)
@SecurityRequirement(name = "bearerAuth")
public interface ReadingStatsControllerDocs {

    @Operation(
            summary = "Lista todos os livros em progresso",
            description = "Retorna uma lista com todos os livros que você ainda está lendo. " +
                    "Leituras finalizadas não aparecem nesta lista."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de livros em progresso retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Reading.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token ausente ou inválido.", content = @Content)
    })
    public ResponseEntity<List<Reading>> getBooksInProgress();

    @Operation(
            summary = "Obtém estatísticas completas de uma leitura",
            description = "Retorna um resumo detalhado de uma leitura..."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas retornadas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReadingStatsDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token ausente ou inválido.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado. A leitura pertence a outro usuário.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada.", content = @Content)
    })
    public ResponseEntity<ReadingStatsDTO> getReadingStats(
            @Parameter(name = "readingId", required = true, example = "1") @PathVariable Long readingId,
            @Parameter(name = "start", example = "2026-03-01T00:00:00") @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(name = "end", example = "2026-03-22T23:59:59") @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(name = "includeOngoing", example = "false") @RequestParam(value = "includeOngoing", required = false, defaultValue = "false") boolean includeOngoing
    );

    @Operation(
            summary = "Obtém a sequência atual de uma leitura",
            description = "Retorna apenas quantos dias seguidos você tem lendo este livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sequência retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "5"))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token ausente ou inválido.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado. A leitura pertence a outro usuário.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada.", content = @Content)
    })
    public ResponseEntity<Integer> getReadingStreak(
            @Parameter(name = "readingId", required = true, example = "1") @PathVariable Long readingId,
            @Parameter(name = "start", example = "2026-03-01T00:00:00") @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(name = "end", example = "2026-03-22T23:59:59") @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    );
}