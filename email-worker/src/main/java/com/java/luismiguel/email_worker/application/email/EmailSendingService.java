package com.java.luismiguel.email_worker.application.email;

import com.java.luismiguel.email_worker.domain.email.EmailRequest;
import com.java.luismiguel.email_worker.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_worker.domain.email.enums.EmailRequestStatus;
import com.java.luismiguel.email_worker.infrastructure.exception.business.email.EmailRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendingService {
    @Value("${spring.app.mail.from}")
    private String from;

    private final EmailRequestRepository emailRequestRepository;
    private final JavaMailSender mailSender;

    public void processEmailSending(UUID emailRequestId) {
        EmailRequest emailRequest = emailRequestRepository.findById(emailRequestId)
                .orElseThrow(EmailRequestNotFoundException::new);

        emailRequest.setAttempts(emailRequest.getAttempts() + 1);
        emailRequest.setStatus(EmailRequestStatus.PROCESSING);

        emailRequestRepository.save(emailRequest);

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            simpleMailMessage.setFrom(from);
            simpleMailMessage.setTo(emailRequest.getRecipient());
            simpleMailMessage.setSubject(emailRequest.getSubject());
            simpleMailMessage.setText(emailRequest.getBody());

            mailSender.send(simpleMailMessage);
            emailRequest.setStatus(EmailRequestStatus.SENT);
            emailRequest.setSentAt(LocalDateTime.now());
            log.info("E-mail sent. E-mail Request Id= {}", emailRequestId);
        } catch (MailException e) {
            emailRequest.setStatus(EmailRequestStatus.FAILED);
            emailRequest.setErrorMessage(e.getMessage());
            log.error("error sending E-mail. E-mail Request Id= {}", emailRequestId);
        }

        emailRequestRepository.save(emailRequest);
    }
}
