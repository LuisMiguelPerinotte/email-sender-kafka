package com.java.luismiguel.email_api.application.service.email;

import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.RetryEmailSendResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.SendEmailResponseDTO;
import com.java.luismiguel.email_api.domain.email.EmailRequest;
import com.java.luismiguel.email_api.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailEventPublishException;
import com.java.luismiguel.email_api.infrastructure.kafka.producer.EmailEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class EmailServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    EmailService emailService;

    @Autowired
    EmailRequestRepository emailRequestRepository;

    @MockitoBean
    private EmailEventProducer emailEventProducer;

    @Nested
    @DisplayName("requestEmailSending")
    class RequestEmailSending {

        SendEmailRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            emailRequestRepository.deleteAll();

            requestDTO = new SendEmailRequestDTO(
                    "recipient",
                    "subject",
                    "body"
            );
        }

        @Test
        @DisplayName("Should Create Email Request Successfully")
        void shouldCreateEmailRequestSuccessfully() {
            // when
            SendEmailResponseDTO responseDTO = emailService.requestEmailSending(requestDTO);

            // then
            assertThat(responseDTO.emailId()).isNotNull();

            Optional<EmailRequest> optionalEmailRequest =
                    emailRequestRepository.findById(responseDTO.emailId());

            assertThat(optionalEmailRequest).isPresent();

            EmailRequest savedEmailRequest = optionalEmailRequest.get();

            assertThat(savedEmailRequest.getRecipient()).isEqualTo(requestDTO.recipient());
            assertThat(savedEmailRequest.getSubject()).isEqualTo(requestDTO.subject());
            assertThat(savedEmailRequest.getBody()).isEqualTo(requestDTO.body());
            assertThat(savedEmailRequest.getAttempts()).isZero();
            assertThat(savedEmailRequest.getStatus()).isEqualTo(EmailRequestStatus.PENDING);

            assertThat(savedEmailRequest.getCreatedAt()).isNotNull();
            assertThat(savedEmailRequest.getUpdatedAt()).isNotNull();

            then(emailEventProducer).should()
                    .publishEmailSendRequested(responseDTO.emailId());
        }


        @Test
        @DisplayName("Should Set Publish Failed When Event Publishing Fails")
        void shouldSetPublishFailedWhenEventPublishingFails() {
            // given
            RuntimeException cause = new RuntimeException("Kafka unavailable");
            willThrow(new EmailEventPublishException("Kafka publish failed", cause))
                    .given(emailEventProducer)
                    .publishEmailSendRequested(any(UUID.class));

            // when
            assertThrows(EmailEventPublishException.class, () -> {
                emailService.requestEmailSending(requestDTO);
            });

            // then
            Optional<EmailRequest> optionalEmailRequest =
                    emailRequestRepository.findAll().stream().findFirst();

            assertThat(optionalEmailRequest).isPresent();
            EmailRequest savedEmailRequest = optionalEmailRequest.get();

            assertThat(savedEmailRequest.getStatus()).isEqualTo(EmailRequestStatus.PUBLISH_FAILED);
            assertThat(savedEmailRequest.getErrorMessage()).isEqualTo("Kafka publish failed");
        }
    }
}
