package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiAskResponseDTO;
import com.timerbook.TimerBook.dto.AiPageTextResponseDTO;
import com.timerbook.TimerBook.dto.AiSearchRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "AI", description = "Endpoints que conectam livros em PDF ao servico Python/Ollama")
public interface AiControllerDocs {

    @Operation(
            summary = "Perguntar sobre um PDF",
            description = "Recebe o ID do livro e uma pergunta. O backend localiza o PDF do livro, envia a pagina informada para o servico Python e retorna a resposta gerada pelo Ollama."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AiAskResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Livro sem PDF ou payload invalido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem permissao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Livro ou PDF nao encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Pagina invalida ou PDF sem texto processavel", content = @Content),
            @ApiResponse(responseCode = "502", description = "Erro retornado pelo servico de IA/Ollama", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servico Python de IA indisponivel", content = @Content)
    })
    ResponseEntity<AiAskResponseDTO> ask(
            @Parameter(description = "Dados da pergunta para o livro", required = true, schema = @Schema(implementation = AiAskRequestDTO.class))
            @Valid @RequestBody AiAskRequestDTO request);

    @Operation(
            summary = "Buscar texto no PDF",
            description = "Recebe o ID do livro e um termo de busca. O backend envia o caminho do PDF ao servico Python, que pesquisa em todas as paginas e retorna pagina + trecho encontrado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca executada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AiSearchResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Livro sem PDF ou payload invalido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem permissao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Livro ou PDF nao encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "PDF invalido ou sem texto processavel", content = @Content),
            @ApiResponse(responseCode = "502", description = "Resposta invalida do servico de IA", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servico Python de IA indisponivel", content = @Content)
    })
    ResponseEntity<AiSearchResponseDTO> search(
            @Parameter(description = "Dados da busca no livro", required = true, schema = @Schema(implementation = AiSearchRequestDTO.class))
            @Valid @RequestBody AiSearchRequestDTO request);

    @Operation(
            summary = "Extrair texto de uma pagina",
            description = "Retorna o texto puro de uma pagina especifica do PDF do livro. E usado pelo modo texto do leitor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Texto extraido com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AiPageTextResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Livro sem PDF ou parametros invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem permissao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Livro ou PDF nao encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Pagina inexistente ou PDF invalido", content = @Content),
            @ApiResponse(responseCode = "502", description = "Resposta invalida do servico de IA", content = @Content),
            @ApiResponse(responseCode = "503", description = "Servico Python de IA indisponivel", content = @Content)
    })
    ResponseEntity<AiPageTextResponseDTO> pageText(
            @Parameter(description = "ID do livro cadastrado no TimerBook", required = true, example = "1")
            @RequestParam @NotNull Long bookId,
            @Parameter(description = "Pagina do PDF, usando numeracao iniciada em 1", required = true, example = "1")
            @RequestParam @NotNull @Positive Integer page);
}
