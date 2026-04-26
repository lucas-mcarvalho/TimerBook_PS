package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.FinishReadingSessionDTO;
import com.timerbook.TimerBook.dto.FinishSessionResponseDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
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

@Tag(
        name = "Sessions",
        description = "Volta para a sessao atual de leitura , exemplo parei na pagina 50 na ultima leitura ,agora inicio uma nova sessao" +
                " com as informacoes da sessao anterior."
)
public interface ReadingSessionControllerDocs {

    @Operation(
            summary = "Inicia uma nova sessao de leitura",
            description = "Passe o id da sessao anterior e a pagina onde parou na ultima sessao."
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
    }) public ResponseEntity<ReadingSession> startReading(@Parameter(
            name = "body",
            description = "Dados para iniciar a nova sessao de leitura. Contém o ID do livro e página onde parou.",
            required = true,
            schema = @Schema(implementation = InitReadingDTO.class)
    )@RequestBody StartReadingSessionDTO dto);


    @Operation(
            summary = "Finaliza uma sessão de leitura",
            description = "Finaliza a sessão de leitura atualizando a página final e a data/hora de término. Retorna a sessão atualizada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessão de leitura finalizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingSession.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida, como sessão não encontrada ou dados incorretos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao finalizar a sessão de leitura",
                    content = @Content(mediaType = "application/json")
            )
    })
    ResponseEntity<FinishSessionResponseDTO> finishReadingSession(
            @Parameter(name = "sessionId", description = "...", required = true)
            @PathVariable Long sessionId,
            @Parameter(name = "body", description = "Página final da sessão", required = true,
                    schema = @Schema(implementation = FinishReadingSessionDTO.class))
            @RequestBody FinishReadingSessionDTO dto);


    @Operation(
            summary = "Busca uma sessão de leitura específica",
            description = "Retorna os detalhes de uma sessão de leitura pelo seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessão de leitura encontrada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingSession.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sessão de leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao buscar a sessão de leitura",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ReadingSession> getSessionById(@PathVariable Long id);

    @Operation(
            summary = "Lista todas as sessões de leitura",
            description = "Retorna uma lista com todas as sessões de leitura registradas no sistema, incluindo as finalizadas e as em andamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de sessões de leitura retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = ReadingSession.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor ao listar as sessões de leitura",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<ReadingSession>> getAllSessions();


    @Operation(
            summary = "Busca todas as sessões de uma leitura específica",
            description = "Retorna uma lista com todas as sessões de leitura de uma leitura específica, incluindo finalizadas e em andamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de sessões retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = ReadingSession.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leitura com o ID fornecido não foi encontrada",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json")
            )
    })  public ResponseEntity<List<ReadingSession>> getSessionsByReadingId(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura para buscar suas sessões",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId);
}
