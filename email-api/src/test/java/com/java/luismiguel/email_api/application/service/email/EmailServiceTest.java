package com.java.luismiguel.email_api.application.service.email;

import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.GetEmailResponseDTO;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    EmailRequestRepository emailRequestRepository;

    @Mock
    EmailEventProducer emailEventProducer;

    @InjectMocks
    EmailService emailService;

    @Nested
    @DisplayName("requestEmailSending")
    class RequestEmailSending {
        UUID emailRequestId;
        SendEmailRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            emailRequestId = UUID.randomUUID();
            requestDTO = new SendEmailRequestDTO(
                    "email@email.com",
                    "subject",
                    "body"
            );
        }

        @Test
        @DisplayName("Should Create Email Request Successfully")
        void shouldCreateEmailRequestSuccessfully() {
            // given
            EmailRequest savedEmail = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient(requestDTO.recipient())
                    .subject(requestDTO.subject())
                    .body(requestDTO.body())
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            given(emailRequestRepository.save(any(EmailRequest.class)))
                    .willReturn(savedEmail);

            // when
            SendEmailResponseDTO response = emailService.requestEmailSending(requestDTO);

            // then
            assertThat(emailRequestId).isEqualTo(response.emailId());
            assertThat(EmailRequestStatus.PENDING).isEqualTo(response.status());


            ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);

            then(emailRequestRepository).should().save(captor.capture());

            EmailRequest email = captor.getValue();

            assertThat(email.getRecipient()).isEqualTo(requestDTO.recipient());
            assertThat(email.getSubject()).isEqualTo(requestDTO.subject());
            assertThat(email.getBody()).isEqualTo(requestDTO.body());
            assertThat(email.getAttempts()).isEqualTo(0);
            assertThat(email.getStatus()).isEqualTo(EmailRequestStatus.PENDING);

            then(emailEventProducer).should()
                    .publishEmailSendRequested(emailRequestId);
        }


        @Test
        @DisplayName("Should Set Publish Failed When Event Publish Fails")
        void shouldSetPublishFailedWhenEventPublishFails() {
            // given
            EmailRequest savedEmail = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient(requestDTO.recipient())
                    .subject(requestDTO.subject())
                    .body(requestDTO.body())
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            given(emailRequestRepository.save(any(EmailRequest.class)))
                    .willReturn(savedEmail);

            RuntimeException cause = new RuntimeException("Kafka unavailable");

            willThrow(new EmailEventPublishException("Kafka publish failed", cause))
                    .given(emailEventProducer)
                    .publishEmailSendRequested(emailRequestId);

            // When + then
            assertThrows(EmailEventPublishException.class, () -> {
                emailService.requestEmailSending(requestDTO);
            });

            assertThat(savedEmail.getStatus()).isEqualTo(EmailRequestStatus.PUBLISH_FAILED);
            assertThat(savedEmail.getErrorMessage()).isEqualTo("Kafka publish failed");

            then(emailRequestRepository).should(times(2)).save(any(EmailRequest.class));
        }
    }


    @Nested
    @DisplayName("getEmailRequest")
    class GetEmailRequest {
        UUID emailRequestId;

        @BeforeEach
        void setUp() {
            emailRequestId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Email Request Does Not Exist")
        void shouldThrowExceptionWhenEmailRequestDoesNotExist() {
            // given
            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.empty());

            // when + then
            EmailRequestNotFoundException exception = assertThrows(EmailRequestNotFoundException.class, () -> {
                emailService.getEmailRequest(emailRequestId);
            });

            assertThat(exception.getMessage()).isEqualTo("Email Request Not Found!");
        }


        @Test
        @DisplayName("Should Get Email Request Successfully")
        void shouldGetEmailRequestSuccessfully() {
            // given
            String recipient = "recipient";
            String subject = "subject";
            EmailRequestStatus status = EmailRequestStatus.SENT;
            Integer attempts = 1;
            String errorMessage = "error message";
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime updatedAt = LocalDateTime.now();
            LocalDateTime sentAt = LocalDateTime.now();

            EmailRequest emailRequest = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient(recipient)
                    .subject(subject)
                    .status(status)
                    .attempts(attempts)
                    .errorMessage(errorMessage)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .sentAt(sentAt)
                    .build();

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(emailRequest));

            // when
            GetEmailResponseDTO response = emailService.getEmailRequest(emailRequestId);

            // then
            assertThat(response.emailRequestId()).isEqualTo(emailRequestId);
            assertThat(response.recipient()).isEqualTo(recipient);
            assertThat(response.subject()).isEqualTo(subject);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.attempts()).isEqualTo(attempts);
            assertThat(response.errorMessage()).isEqualTo(errorMessage);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
            assertThat(response.sentAt()).isEqualTo(sentAt);
        }
    }


    @Nested
    @DisplayName("retryEmail")
    class RetryEmail {
        UUID emailRequestId;

        @BeforeEach
        void setUp() {
            emailRequestId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should retry email successfully when status is failed")
        void shouldRetryEmailSuccessfullyWhenStatusIsFailed() {
            // given
            EmailRequest email = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .status(EmailRequestStatus.FAILED)
                    .errorMessage("SMTP error")
                    .build();

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(email));

            // when
            RetryEmailSendResponseDTO response = emailService.retryEmail(emailRequestId);

            // then
            assertThat(response.emailId()).isEqualTo(emailRequestId);
            assertThat(response.status()).isEqualTo(EmailRequestStatus.PENDING);

            assertThat(email.getStatus()).isEqualTo(EmailRequestStatus.PENDING);
            assertThat(email.getErrorMessage()).isNull();

            then(emailRequestRepository).should().save(email);
            then(emailEventProducer).should().publishEmailSendRequested(emailRequestId);
        }


        @Test
        @DisplayName("Should throw exception when email status is not retryable")
        void shouldThrowExceptionWhenEmailStatusIsNotRetryable() {
            // given
            EmailRequest email = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(email));

            // when + then
            EmailCannotBeRetryableException exception = assertThrows(EmailCannotBeRetryableException.class, () -> {
                emailService.retryEmail(emailRequestId);
            });

            assertThat(exception.getMessage()).isEqualTo("E-mail Cannot Be Retryable!");

            then(emailRequestRepository).should(never()).save(any(EmailRequest.class));
            then(emailEventProducer).shouldHaveNoInteractions();
        }


        @Test
        @DisplayName("Should throw exception when retry email does not exist")
        void shouldThrowExceptionWhenRetryEmailDoesNotExist() {
            // given
            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.empty());

            // when + then
            EmailRequestNotFoundException exception = assertThrows(EmailRequestNotFoundException.class, () -> {
                emailService.retryEmail(emailRequestId);
            });

            assertThat(exception.getMessage()).isEqualTo("Email Request Not Found!");

            then(emailRequestRepository).should(never()).save(any(EmailRequest.class));
            then(emailEventProducer).shouldHaveNoInteractions();
        }


        @Test
        @DisplayName("Should set publish failed when retry publishing fails")
        void shouldSetPublishFailedWhenRetryPublishingFails() {
            // given
            EmailRequest email = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .status(EmailRequestStatus.FAILED)
                    .errorMessage("SMTP error")
                    .build();

            RuntimeException cause = new RuntimeException("Kafka unavailable");

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(email));

            willThrow(new EmailEventPublishException("Kafka publish failed", cause))
                    .given(emailEventProducer)
                    .publishEmailSendRequested(emailRequestId);

            // when + then
            assertThrows(EmailEventPublishException.class, () -> {
                emailService.retryEmail(emailRequestId);
            });

            assertThat(email.getStatus()).isEqualTo(EmailRequestStatus.PUBLISH_FAILED);
            assertThat(email.getErrorMessage()).isEqualTo("Kafka publish failed");

            then(emailRequestRepository).should(times(2)).save(email);
        }
    }
}
