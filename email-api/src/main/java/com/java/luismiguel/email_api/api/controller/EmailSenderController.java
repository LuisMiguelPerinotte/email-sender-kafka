package com.java.luismiguel.email_api.api.controller;

import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.GetEmailResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.SendEmailResponseDTO;
import com.java.luismiguel.email_api.application.service.email.EmailSenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailSenderController {
    private final EmailSenderService emailSenderService;

    @PostMapping("/send")
    public ResponseEntity<SendEmailResponseDTO> sendEmail(@Valid @RequestBody SendEmailRequestDTO requestDTO) {
        return new ResponseEntity<>(emailSenderService.requestEmailSending(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetEmailResponseDTO> getEmail(@PathVariable UUID id) {
        return new ResponseEntity<>(emailSenderService.getEmailRequest(id), HttpStatus.OK);
    }
}
