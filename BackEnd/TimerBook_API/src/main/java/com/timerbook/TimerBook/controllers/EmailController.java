package com.timerbook.TimerBook.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timerbook.TimerBook.controllers.docs.EmailControllerDocs;
import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.services.EmailService;

@RestController
@RequestMapping("/email")
public class EmailController implements EmailControllerDocs {
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    @Override
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDTO emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            return new ResponseEntity<>("Email sent successfully!", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>("Failed to send e-mail: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
