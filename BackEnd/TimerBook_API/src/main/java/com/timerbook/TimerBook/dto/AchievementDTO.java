package com.timerbook.TimerBook.dto;

public class AchievementDTO {

    private String nome;
    private String icone;

    public AchievementDTO() {}

    public AchievementDTO(String nome, String icone) {
        this.nome = nome;
        this.icone = icone;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }
}