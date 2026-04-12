package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;
@Schema(description = "Dados do usuário")
public class UserDTO {

    @Schema(description = "Nome do usuário", example = "João")
    private String username;
    @Schema(description = "Email do usuário", example = "joao@email.com")
    private String email;
    @Schema(description = "Senha do usuário", example = "123456")
    private String password;
    @Schema(description = "Foto do usuário")
    private MultipartFile photopath;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MultipartFile getPhotopath() {
        return photopath;
    }

    public void setPhotopath(MultipartFile photopath) {
        this.photopath = photopath;
    }
}
