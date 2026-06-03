package com.java.luismiguel.email_api.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.GetEmailResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.RetryEmailSendResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.SendEmailResponseDTO;
import com.java.luismiguel.email_api.application.service.email.EmailService;
import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailSenderController.class)
public class EmailSenderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    UUID emailRequestId;

    @BeforeEach
    void setUp() {
        emailRequestId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should Create Email Request")
    void shouldCreateEmailRequest() throws Exception {
        SendEmailRequestDTO request = new SendEmailRequestDTO(
                "user@email.com",
                "Hello",
                "Testing Kafka"
        );

        given(emailService.requestEmailSending(any(SendEmailRequestDTO.class)))
                .willReturn(new SendEmailResponseDTO(emailRequestId, EmailRequestStatus.PENDING));

        mockMvc.perform(post("/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailId").value(emailRequestId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    @Test
    @DisplayName("Should Return Email Request By Id")
    void shouldReturnEmailRequestById() throws Exception {
        String recipient = "recipient";
        String subject = "subject";
        EmailRequestStatus status = EmailRequestStatus.SENT;
        Integer attempts = 1;
        String errorMessage = "";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime sentAt = LocalDateTime.now();

        GetEmailResponseDTO responseDTO = new GetEmailResponseDTO(
                emailRequestId,
                recipient,
                subject,
                status,
                attempts,
                errorMessage,
                createdAt,
                updatedAt,
                sentAt
        );

        given(emailService.getEmailRequest(emailRequestId))
                .willReturn(responseDTO);

        mockMvc.perform(get("/email/{id}", emailRequestId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailRequestId").value(emailRequestId.toString()))
                .andExpect(jsonPath("$.recipient").value(recipient))
                .andExpect(jsonPath("$.subject").value(subject))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.attempts").value(attempts))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.sentAt").exists());
    }


    @Test
    @DisplayName("Should Retry Email Request Successfully")
    void shouldRetryEmailRequestSuccessfully() throws Exception{
        given(emailService.retryEmail(emailRequestId))
                .willReturn(new RetryEmailSendResponseDTO(emailRequestId, EmailRequestStatus.PENDING));

        mockMvc.perform(post("/email/{id}/retry", emailRequestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.emailId").value(emailRequestId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
