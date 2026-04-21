package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Dados para criação/atualização de livro")
public class BookDTO {

    @Schema(description = "Nome do livro", example = "Senhor dos Anéis")
    private String name;
    @Schema(description = "Descrição do livro")
    private String description;

    @Schema(description = "Imagem de capa")
    private MultipartFile cover;

    @Schema(description = "Arquivo PDF")
    private MultipartFile data;

    public BookDTO(){

    }


    public BookDTO(String name, String description, MultipartFile cover, MultipartFile data) {
        this.name = name;
        this.description = description;
        this.cover = cover;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getCover() {
        return cover;
    }

    public void setCover(MultipartFile cover) {
        this.cover = cover;
    }

    public MultipartFile getData() {
        return data;
    }

    public void setData(MultipartFile data) {
        this.data = data;
    }
}
