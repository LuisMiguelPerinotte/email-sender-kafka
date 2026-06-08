package com.java.luismiguel.email_api.application.service.email;

import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.RetryEmailSendResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.SendEmailResponseDTO;
import com.java.luismiguel.email_api.domain.email.EmailRequest;
import com.java.luismiguel.email_api.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailCannotBeRetryableException;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailEventPublishException;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailRequestNotFoundException;
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


    @Nested
    @DisplayName("retryEmail")
    class RetryEmail {

        @BeforeEach
        void setUp() {
            emailRequestRepository.deleteAll();
        }

        @Test
        @DisplayName("Should Retry Failed Email Successfully")
        void shouldRetryFailedEmailSuccessfully() {
            // given
            EmailRequest failedEmailRequest =
                    EmailRequest.builder()
                            .recipient("recipient")
                            .subject("subject")
                            .body("body")
                            .attempts(4)
                            .errorMessage("SMTP error")
                            .status(EmailRequestStatus.FAILED)
                            .build();

            EmailRequest savedFailedEmailRequest = emailRequestRepository.save(failedEmailRequest);

            // when
            RetryEmailSendResponseDTO responseDTO =
                    emailService.retryEmail(savedFailedEmailRequest.getEmailRequestId());

            // then
            assertThat(responseDTO.emailId()).isNotNull();
            assertThat(responseDTO.status()).isEqualTo(EmailRequestStatus.PENDING);

            Optional<EmailRequest> optionalEmailRequest =
                    emailRequestRepository.findById(responseDTO.emailId());

            assertThat(optionalEmailRequest).isPresent();

            EmailRequest savedEmailRequest = optionalEmailRequest.get();

            assertThat(savedEmailRequest.getErrorMessage()).isNull();
            assertThat(savedEmailRequest.getStatus()).isEqualTo(EmailRequestStatus.PENDING);

            then(emailEventProducer).should()
                    .publishEmailSendRequested(responseDTO.emailId());
        }


        @Test
        @DisplayName("Should Retry Publish Failed Email Successfully")
        void shouldRetryPublishFailedEmailSuccessfully() {
            // given
            EmailRequest publishFailedEmailRequest =
                    EmailRequest.builder()
                            .recipient("recipient")
                            .subject("subject")
                            .body("body")
                            .attempts(4)
                            .errorMessage("publish failed")
                            .status(EmailRequestStatus.PUBLISH_FAILED)
                            .build();

            EmailRequest savedFailedEmailRequest = emailRequestRepository.save(publishFailedEmailRequest);

            // when
            RetryEmailSendResponseDTO responseDTO =
                    emailService.retryEmail(savedFailedEmailRequest.getEmailRequestId());

            // then
            assertThat(responseDTO.emailId()).isNotNull();
            assertThat(responseDTO.status()).isEqualTo(EmailRequestStatus.PENDING);

            Optional<EmailRequest> optionalEmailRequest =
                    emailRequestRepository.findById(responseDTO.emailId());

            assertThat(optionalEmailRequest).isPresent();

            EmailRequest savedEmailRequest = optionalEmailRequest.get();

            assertThat(savedEmailRequest.getErrorMessage()).isNull();

            then(emailEventProducer).should()
                    .publishEmailSendRequested(responseDTO.emailId());
        }


        @Test
        @DisplayName("Should Throw Exception When Email Status Is Not Retryable")
        void shouldThrowExceptionWhenEmailStatusIsNotRetryable() {
            // given
            EmailRequest emailRequest =
                    EmailRequest.builder()
                            .recipient("recipient")
                            .subject("subject")
                            .body("body")
                            .attempts(1)
                            .status(EmailRequestStatus.SENT)
                            .build();

            EmailRequest savedEmailRequest = emailRequestRepository.save(emailRequest);

            // when + then
            assertThrows(EmailCannotBeRetryableException.class, () -> {
                emailService.retryEmail(savedEmailRequest.getEmailRequestId());
            });

            then(emailEventProducer).shouldHaveNoInteractions();
        }


        @Test
        @DisplayName("Should Throw Exception When Email Request Does Not Exist")
        void shouldThrowExceptionWhenEmailRequestDoesNotExist() {
            // when + then
            assertThrows(EmailRequestNotFoundException.class, () -> {
                emailService.retryEmail(UUID.randomUUID());
            });
        }


        @Test
        @DisplayName("Should Set Publish Failed When Retry Publishing Fails")
        void shouldSetPublishFailedWhenRetryPublishingFails() {
            // given
            EmailRequest emailRequest = EmailRequest.builder()
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .attempts(1)
                    .status(EmailRequestStatus.FAILED)
                    .errorMessage("SMTP error")
                    .build();

            EmailRequest savedEmail = emailRequestRepository.save(emailRequest);

            RuntimeException cause = new RuntimeException("Kafka unavailable");

            willThrow(new EmailEventPublishException("Kafka publish failed", cause))
                    .given(emailEventProducer)
                    .publishEmailSendRequested(savedEmail.getEmailRequestId());

            // when + then
            assertThrows(EmailEventPublishException.class, () -> {
                emailService.retryEmail(savedEmail.getEmailRequestId());
            });

            Optional<EmailRequest> optionalEmailRequest =
                    emailRequestRepository.findById(savedEmail.getEmailRequestId());

            assertThat(optionalEmailRequest).isPresent();

            EmailRequest updatedEmail = optionalEmailRequest.get();

            assertThat(updatedEmail.getStatus()).isEqualTo(EmailRequestStatus.PUBLISH_FAILED);
            assertThat(updatedEmail.getErrorMessage()).isEqualTo("Kafka publish failed");

            then(emailEventProducer).should()
                    .publishEmailSendRequested(savedEmail.getEmailRequestId());
        }
    }
}
