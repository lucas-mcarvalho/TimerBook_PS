package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.services.ReadingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reading-sessions")
@Tag(
        name = "Sessions",
        description = "Volta para a sessao atual de leitura , exemplo parei na pagina 50 na ultima leitura ,agora inicio uma nova sessao" +
                " com as informacoes da sessao anterior."
)
public class ReadingSessionController {

    @Autowired
    private ReadingSessionService readingSessionService;
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
    })
    @PostMapping("/start")
    public ResponseEntity<ReadingSession> startReading(   @Parameter(
            name = "body",
            description = "Dados para iniciar a nova sessao de leitura. Contém o ID do livro e página onde parou.",
            required = true,
            schema = @Schema(implementation = InitReadingDTO.class)
    )@RequestBody StartReadingSessionDTO dto) {

        try {
            ReadingSession session = readingSessionService.startReadingSession(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}