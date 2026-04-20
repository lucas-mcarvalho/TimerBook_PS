package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.UserDTO;
import com.timerbook.TimerBook.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "Api de usuários")

public interface UserControllerDocs {
    @Operation(summary = "Criar usuário via JSON", description = "Cria um usuário sem foto usando JSON")
    public ResponseEntity<User> createFromJson(@RequestBody UserDTO dto);
    @Operation(
            summary = "Criar usuário",
            description = "Cria um usuário com foto opcional"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    }) public ResponseEntity<User> create(
            @Parameter(description = "Nome do usuário", example = "João")
            @RequestPart("username") String username,

            @Parameter(description = "Email do usuário", example = "joao@email.com")
            @RequestPart("email") String email,

            @Parameter(description = "Senha do usuário", example = "123456")
            @RequestPart("password") String password,

            @Parameter(description = "Foto do usuário")
            @RequestPart(value = "photo", required = false) MultipartFile photo);

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<User> getById(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id);


    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @RequestPart("username") String username,
            @RequestPart("email") String email,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "removePhoto", required = false) String removePhotoStr);


    @Operation(summary = "Deletar usuário")
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id);

}
