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
    @Schema(description = "Verifica se tem foto")
    private Boolean  removePhoto;
    @Schema(description = "Meta diária de leitura em minutos", example = "10")
    private Integer dailyReadingGoalMinutes;

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

    public Boolean getRemovePhoto() {
        return removePhoto;
    }

    public void setRemovePhoto(Boolean removePhoto) {
        this.removePhoto = removePhoto;
    }

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }
}
