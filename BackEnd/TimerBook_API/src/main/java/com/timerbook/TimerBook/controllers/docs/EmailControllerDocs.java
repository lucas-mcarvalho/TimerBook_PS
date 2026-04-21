package com.timerbook.TimerBook.controllers.docs;

import org.springframework.http.ResponseEntity;

import com.timerbook.TimerBook.dto.EmailRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface EmailControllerDocs {
    @Operation(summary = "Send an e-Mail",
            description = "Sends an e-mail by providing details, subject and body!",
            tags = {"e-Mail"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<String> sendEmail(EmailRequestDTO emailRequestDTO);
}
