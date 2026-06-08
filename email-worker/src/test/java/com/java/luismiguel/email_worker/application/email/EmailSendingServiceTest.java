package com.java.luismiguel.email_worker.application.email;

import com.java.luismiguel.email_worker.domain.email.EmailRequest;
import com.java.luismiguel.email_worker.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_worker.domain.email.enums.EmailRequestStatus;
import com.java.luismiguel.email_worker.infrastructure.exception.business.email.EmailRequestNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class EmailSendingServiceTest {
    @Mock
    EmailRequestRepository emailRequestRepository;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailSendingService emailSendingService;

    @Nested
    @DisplayName("processEmailSending")
    class ProcessEmailSending {
        UUID emailRequestId;

        @BeforeEach
        void setUp() {
            emailRequestId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Email Request Not Found")
        void shouldThrowExceptionWhenEmailRequestNotFound() {
            // given
            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.empty());

            // when + then
            EmailRequestNotFoundException exception = assertThrows(EmailRequestNotFoundException.class, () -> {
                emailSendingService.processEmailSending(emailRequestId);
            });

            assertThat(exception.getMessage()).isEqualTo("Email Request Not Found!");
        }


        @Test
        @DisplayName("Should Send Email Successfully")
        void shouldSendEmailSuccessfully() {
            // given
            EmailRequest emailRequest = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(emailRequest));

            // when
            emailSendingService.processEmailSending(emailRequestId);

            // then
            assertThat(emailRequest.getAttempts()).isEqualTo(1);
            assertThat(emailRequest.getStatus()).isEqualTo(EmailRequestStatus.SENT);
            assertThat(emailRequest.getSentAt()).isNotNull();

            then(mailSender).should().send(any(SimpleMailMessage.class));
            then(emailRequestRepository).should(times(2)).save(emailRequest);
        }


        @Test
        @DisplayName("Should Set Failed When Mail Sender Throws Exception")
        void shouldSetFailedWhenMailSenderThrowsException() {
            // given
            EmailRequest emailRequest = EmailRequest.builder()
                    .EmailRequestId(emailRequestId)
                    .recipient("user@email.com")
                    .subject("Hello")
                    .body("Testing Kafka")
                    .attempts(0)
                    .status(EmailRequestStatus.PENDING)
                    .build();

            given(emailRequestRepository.findById(emailRequestId))
                    .willReturn(Optional.of(emailRequest));


            willThrow(new MailSendException("SMTP unavailable"))
                    .given(mailSender)
                    .send(any(SimpleMailMessage.class));

            assertThrows(MailSendException.class, () -> {
                emailSendingService.processEmailSending(emailRequestId);
            });

            assertThat(emailRequest.getErrorMessage()).isEqualTo("SMTP unavailable");
            assertThat(emailRequest.getStatus()).isEqualTo(EmailRequestStatus.FAILED);
            assertThat(emailRequest.getAttempts()).isEqualTo(1);

            then(emailRequestRepository).should(times(2)).save(emailRequest);
        }
    }
}
