package com.timerbook.TimerBook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class AiOllamaService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String ollamaUrl;
    private final String model;

    public AiOllamaService(
            ObjectMapper objectMapper,
            @Value("${app.ai.ollama.url:http://localhost:11434/api/generate}") String ollamaUrl,
            @Value("${app.ai.ollama.model:qwen2.5:14b}") String model
    ) {
        this.objectMapper = objectMapper;
        this.ollamaUrl = ollamaUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String askModel(String context, String question) {
        String prompt = """
                Você é um assistente de leitura. Use apenas o conteúdo abaixo para responder.

                CONTEÚDO DO DOCUMENTO:
                %s

                PERGUNTA: %s

                Responda de forma clara e baseada somente no conteúdo fornecido.
                """.formatted(context, question);

        return sendPrompt(prompt);
    }

    public String translateText(String text, String targetLanguage) {
        String prompt = """
                Traduza o texto abaixo para %s.
                Preserve o significado, a estrutura e a separação de parágrafos.
                Não adicione comentários, explicações, títulos ou notas.

                TEXTO:
                %s
                """.formatted(targetLanguage, text);

        return sendPrompt(prompt);
    }

    private String sendPrompt(String prompt) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            ));

            HttpRequest request = HttpRequest.newBuilder(URI.create(ollamaUrl))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Erro ao consultar o Ollama: HTTP " + response.statusCode()
                );
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode answer = json.get("response");

            if (answer == null || answer.asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O Ollama não retornou uma resposta válida.");
            }

            return answer.asText();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A consulta ao Ollama foi interrompida.", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível consultar o Ollama.", exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "URL do Ollama inválida.", exception);
        }
    }
}
