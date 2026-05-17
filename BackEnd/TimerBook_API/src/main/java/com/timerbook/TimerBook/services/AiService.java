package com.timerbook.TimerBook.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiAskResponseDTO;
import com.timerbook.TimerBook.dto.AiPageTextResponseDTO;
import com.timerbook.TimerBook.dto.AiSearchRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResponseDTO;
import com.timerbook.TimerBook.models.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiService {

    private final BookService bookService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiService(
            BookService bookService,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.ai.base-url:http://localhost:8000}") String aiBaseUrl,
            @Value("${app.ai.timeout-seconds:420}") int timeoutSeconds
    ) {
        this.bookService = bookService;
        this.objectMapper = objectMapper;

        int timeoutMillis = timeoutSeconds * 1000;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        this.restClient = restClientBuilder
                .baseUrl(aiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public AiAskResponseDTO ask(AiAskRequestDTO request) {
        String pdfPath = resolvePdfPath(request.getBookId());
        PythonAskRequest pythonRequest = new PythonAskRequest(
                pdfPath,
                request.getQuestion(),
                request.getPage(),
                null,
                null
        );

        AiAskResponseDTO response = callAiService(() -> restClient.post()
                .uri("/api/v1/ask")
                .body(pythonRequest)
                .retrieve()
                .body(AiAskResponseDTO.class));

        if (response == null || response.getAnswer() == null || response.getAnswer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta invalida do servico de IA.");
        }

        return response;
    }

    public AiSearchResponseDTO search(AiSearchRequestDTO request) {
        String pdfPath = resolvePdfPath(request.getBookId());
        PythonSearchRequest pythonRequest = new PythonSearchRequest(pdfPath, request.getQuery());

        AiSearchResponseDTO response = callAiService(() -> restClient.post()
                .uri("/api/v1/search")
                .body(pythonRequest)
                .retrieve()
                .body(AiSearchResponseDTO.class));

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta invalida do servico de IA.");
        }

        return response;
    }

    public AiPageTextResponseDTO pageText(Long bookId, Integer page) {
        String pdfPath = resolvePdfPath(bookId);

        AiPageTextResponseDTO response = callAiService(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/page-text")
                        .queryParam("pdf_path", pdfPath)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(AiPageTextResponseDTO.class));

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta invalida do servico de IA.");
        }

        return response;
    }

    private String resolvePdfPath(Long bookId) {
        Book book = bookService.findById(bookId);
        String dataPath = book.getDataPath();

        if (dataPath == null || dataPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Livro nao possui PDF cadastrado.");
        }

        return dataPath;
    }

    private <T> T callAiService(AiCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException exception) {
            throw toResponseStatusException(exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servico de IA indisponivel.", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Erro ao chamar o servico de IA.", exception);
        }
    }

    private ResponseStatusException toResponseStatusException(RestClientResponseException exception) {
        HttpStatus status = switch (exception.getStatusCode().value()) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.BAD_GATEWAY;
        };

        return new ResponseStatusException(status, extractErrorDetail(exception));
    }

    private String extractErrorDetail(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return "Erro no servico de IA.";
        }

        try {
            JsonNode detail = objectMapper.readTree(responseBody).get("detail");
            if (detail != null && detail.isTextual()) {
                return detail.asText();
            }
        } catch (JsonProcessingException ignored) {
            return responseBody;
        }

        return responseBody;
    }

    @FunctionalInterface
    private interface AiCall<T> {
        T execute();
    }

    private record PythonAskRequest(
            @JsonProperty("pdf_path") String pdfPath,
            String question,
            Integer page,
            @JsonProperty("start_page") Integer startPage,
            @JsonProperty("end_page") Integer endPage
    ) {
    }

    private record PythonSearchRequest(
            @JsonProperty("pdf_path") String pdfPath,
            String query
    ) {
    }
}
