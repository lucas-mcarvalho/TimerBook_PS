package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta gerada pelo assistente de leitura")
public class AiAskResponseDTO {

    @Schema(description = "Resposta baseada no texto extraido do PDF", example = "O documento trata de conceitos de roteamento IP e encaminhamento de pacotes em redes.")
    private String answer;

    public AiAskResponseDTO() {
    }

    public AiAskResponseDTO(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
