package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.services.ReadingStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/stats")
@Tag(
        name = "Statistics",
        description = "Consulta estatísticas e métricas de leitura: páginas lidas, tempo gasto, sequências e muito mais"
)
public class ReadingStatsController {

    @Autowired
    private ReadingStatsService service;

    @GetMapping("/books-in-progress")
    @Operation(
            summary = "Lista todos os livros em progresso",
            description = "Retorna uma lista com todos os livros que você ainda está lendo. " +
                    "Leituras finalizadas não aparecem nesta lista. " +
                    "Use para ver seu progresso atual em cada livro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de livros em progresso retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reading.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao recuperar livros em progresso",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<Reading>> getBooksInProgress() {
        List<Reading> readings = service.getReadingsInProgress();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/reading/{readingId}")
    @Operation(
            summary = "Obtém estatísticas completas de uma leitura",
            description = "Retorna um resumo detalhado de uma leitura com as seguintes informações:\n" +
                    "- Total de páginas lidas\n" +
                    "- Tempo total gasto lendo (em segundos)\n" +
                    "- Número de sessões de leitura\n" +
                    "- Tempo médio por sessão\n" +
                    "- Quantos dias seguidos você leu (sequência atual)\n" +
                    "- Maior sequência de dias seguidos (recorde)\n\n" +
                    "Você pode filtrar por período usando os parâmetros 'start' e 'end'."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas retornadas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingStatsDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao calcular estatísticas",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ReadingStatsDTO> getReadingStats(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura para obter estatísticas",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId,
            @Parameter(
                    name = "start",
                    description = "Data/hora inicial para filtrar (formato ISO 8601). " +
                            "Exemplo: 2026-03-01T00:00:00. Se não informado, usa 01/01/2010.",
                    example = "2026-03-01T00:00:00",
                    required = false
            )
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(
                    name = "end",
                    description = "Data/hora final para filtrar (formato ISO 8601). " +
                            "Exemplo: 2026-03-22T23:59:59. Se não informado, usa data/hora atual.",
                    example = "2026-03-22T23:59:59",
                    required = false
            )
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(
                    name = "includeOngoing",
                    description = "Se 'true', inclui sessões que ainda estão em andamento (sem data de término). " +
                            "Se 'false' (padrão), conta apenas sessões concluídas.",
                    example = "false",
                    required = false
            )
            @RequestParam(value = "includeOngoing", required = false, defaultValue = "false") boolean includeOngoing
    ) {
        ReadingStatsDTO dto = service.getStatsForReading(readingId, start, end, includeOngoing);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reading/{readingId}/streak")
    @Operation(
            summary = "Obtém a sequência atual de uma leitura",
            description = "Retorna apenas quantos dias seguidos você tem lendo este livro. " +
                    "Use este endpoint se só quiser saber a sequência, sem outras estatísticas. " +
                    "Se voltou a ler depois de alguns dias sem ler, a sequência atual recomeça."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sequência retornada com sucesso (número inteiro de dias)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer", example = "5")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao calcular a sequência",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Integer> getReadingStreak(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura para obter a sequência",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId,
            @Parameter(
                    name = "start",
                    description = "Data/hora inicial para cálculo (formato ISO 8601). Se não informado, usa 01/01/2010.",
                    example = "2026-03-01T00:00:00",
                    required = false
            )
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(
                    name = "end",
                    description = "Data/hora final para cálculo (formato ISO 8601). Se não informado, usa data/hora atual.",
                    example = "2026-03-22T23:59:59",
                    required = false
            )
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        ReadingStatsDTO dto = service.getStatsForReading(readingId, start, end, false);
        return ResponseEntity.ok(dto.getCurrentStreakDays());
    }
}