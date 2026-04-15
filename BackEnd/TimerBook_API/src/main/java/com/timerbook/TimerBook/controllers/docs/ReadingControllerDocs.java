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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(
        name = "Readings",
        description = "Gerencia o ciclo de vida completo de uma leitura: iniciar, registrar progresso e finalizar"
)
public interface ReadingControllerDocs {

    @Operation(
            summary = "Inicia uma nova leitura",
            description = "Cria uma nova leitura para um livro específico e inicia automaticamente uma sessão de leitura. " +
                    "A leitura começa com a data/hora atual. " +
                    "Retorna o ID da leitura para usar nas próximas ações."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Leitura e sessão criadas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reading.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro com o ID fornecido não foi encontrado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao criar a leitura",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Reading> startReading(
            @Parameter(
                    name = "body",
                    description = "Dados para iniciar a leitura. Contém o ID do livro e página inicial (opcional).",
                    required = true,
                    schema = @Schema(implementation = InitReadingDTO.class)
            )
            @RequestBody InitReadingDTO dto);



    @Operation(
            summary = "Finaliza uma leitura",
            description = "Marca uma leitura como concluída, registrando a data/hora final e opcionalmente a página final. " +
                    "A leitura deixará de aparecer em 'livros em progresso' após ser finalizada. " +
                    "Retorna os dados atualizados da leitura."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitura finalizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reading.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao finalizar a leitura",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Reading> finishReading(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura a ser finalizada",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId,
            @Parameter(
                    name = "body",
                    description = "Dados para finalizar: página final e observações (opcional)",
                    required = true,
                    schema = @Schema(implementation = FinishReadingDTO.class)
            )
            @RequestBody FinishReadingDTO dto);


    @Operation(
            summary = "Obtém os dados de uma leitura",
            description = "Retorna os dados completos de uma leitura específica, incluindo livro associado, " +
                    "página atual, data de início e data de término (se houver). " +
                    "Funciona tanto para leituras em andamento quanto concluídas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leitura retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reading.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao recuperar a leitura",
                    content = @Content(mediaType = "application/json")
            )
    })  public ResponseEntity<Reading> getReading(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura a ser obtida",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId);

    @GetMapping("/book/{bookId}")
    @Operation(
            summary = "Busca todas as leituras de um livro",
            description = "Retorna uma lista com todas as leituras (períodos de leitura) de um livro específico, incluindo leituras finalizadas e em andamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de leituras retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Reading.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro com o ID fornecido não foi encontrado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<Reading>> getReadingsByBookId(
            @Parameter(
                    name = "bookId",
                    description = "ID do livro para buscar suas leituras",
                    required = true,
                    example = "1"
            )
            @PathVariable Long bookId);

    @Operation(
            summary = "Lista todas as leituras",
            description = "Retorna uma lista com todas as leituras registradas no sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de leituras retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = Reading.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<Reading>> getAllReadings();
}
