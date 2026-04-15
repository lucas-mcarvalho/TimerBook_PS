package com.timerbook.TimerBook.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.services.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Operation(summary = "Envia um email",
            description = "Envia um email fornecendo o destinatário, assunto e mensagem no corpo da requisição.",
            tags = {"e-Mail"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDTO emailRequest) {
        emailService.sendEmail(emailRequest);
        return new ResponseEntity<>("Email sent successfully!", HttpStatus.OK);
    }
}
