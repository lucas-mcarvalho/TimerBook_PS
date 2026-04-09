package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.UserDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.services.UserService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "Api de usuários")
public class UserController {
    @Autowired
    private  UserService userService;


    @Operation(summary = "Criar usuário via JSON", description = "Cria um usuário sem foto usando JSON")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createFromJson(@RequestBody UserDTO dto) {
        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }


    @Operation(
            summary = "Criar usuário",
            description = "Cria um usuário com foto opcional"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> create(
            @Parameter(description = "Nome do usuário", example = "João")
            @RequestPart("username") String username,

            @Parameter(description = "Email do usuário", example = "joao@email.com")
            @RequestPart("email") String email,

            @Parameter(description = "Senha do usuário", example = "123456")
            @RequestPart("password") String password,

            @Parameter(description = "Foto do usuário")
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setPhotopath(photo);

        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> update(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id,

            @RequestPart("username") String username,
            @RequestPart("email") String email,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPhotopath(photo);

        return ResponseEntity.ok(userService.update(id, dto));
    }

    @Operation(summary = "Deletar usuário")
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long id) {

        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}