package com.java.luismiguel.email_worker.application.email;

import com.java.luismiguel.email_worker.domain.email.EmailRequest;
import com.java.luismiguel.email_worker.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_worker.domain.email.enums.EmailRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class EmailSendingServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    EmailSendingService emailSendingService;

    @Autowired
    EmailRequestRepository emailRequestRepository;

    @MockitoBean
    JavaMailSender mailSender;

    @Nested
    @DisplayName("processEmailSending")
    class ProcessEmailSending {

        @BeforeEach
        void setUp() {
            emailRequestRepository.deleteAll();
        }

        @Test
        @DisplayName("Should send email successfully")
        void shouldSendEmailSuccessfully() {
            EmailRequest email = EmailRequest.builder()
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            EmailRequest savedEmail = emailRequestRepository.save(email);

            emailSendingService.processEmailSending(savedEmail.getEmailRequestId());

            EmailRequest result = emailRequestRepository.findById(savedEmail.getEmailRequestId())
                    .orElseThrow();

            assertThat(result.getAttempts()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo(EmailRequestStatus.SENT);
            assertThat(result.getSentAt()).isNotNull();
            assertThat(result.getErrorMessage()).isNull();

            then(mailSender).should().send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should set failed when mail sender fails")
        void shouldSetFailedWhenMailSenderFails() {
            EmailRequest email = EmailRequest.builder()
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            EmailRequest savedEmail = emailRequestRepository.save(email);

            willThrow(new MailSendException("SMTP unavailable"))
                    .given(mailSender)
                    .send(any(SimpleMailMessage.class));

            assertThrows(MailSendException.class, () -> {
                emailSendingService.processEmailSending(savedEmail.getEmailRequestId());
            });

            EmailRequest result = emailRequestRepository.findById(savedEmail.getEmailRequestId())
                    .orElseThrow();

            assertThat(result.getAttempts()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo(EmailRequestStatus.FAILED);
            assertThat(result.getErrorMessage()).isEqualTo("SMTP unavailable");
            assertThat(result.getSentAt()).isNull();

            then(mailSender).should().send(any(SimpleMailMessage.class));
        }
    }
}
