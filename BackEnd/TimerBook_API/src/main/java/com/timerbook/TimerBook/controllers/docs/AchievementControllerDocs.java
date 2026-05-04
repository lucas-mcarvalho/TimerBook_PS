package com.timerbook.TimerBook.controllers.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.timerbook.TimerBook.dto.AchievementDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Achievements", description = "API para gerenciar conquistas e medalhas")

public interface AchievementControllerDocs {

    @Operation(summary = "Obter conquistas de um usuário")
        @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Conquistas obtidas com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = AchievementDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID do usuário inválido")
        })
        public ResponseEntity<List<AchievementDTO>> getUserMedals(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId
        );

}
