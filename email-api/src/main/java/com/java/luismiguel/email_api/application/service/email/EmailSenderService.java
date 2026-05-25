package com.java.luismiguel.email_api.application.service.email;

import com.java.luismiguel.email_api.api.dto.email.request.SendEmailRequestDTO;
import com.java.luismiguel.email_api.api.dto.email.response.GetEmailResponseDTO;
import com.java.luismiguel.email_api.api.dto.email.response.SendEmailResponseDTO;
import com.java.luismiguel.email_api.domain.email.EmailRequest;
import com.java.luismiguel.email_api.domain.email.EmailRequestRepository;
import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailEventPublishException;
import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailRequestNotFoundException;
import com.java.luismiguel.email_api.infrastructure.kafka.producer.EmailEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final EmailRequestRepository emailRequestRepository;
    private final EmailEventProducer emailEventProducer;

    public SendEmailResponseDTO requestEmailSending (SendEmailRequestDTO requestDTO) {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipient(requestDTO.recipient())
                .subject(requestDTO.subject())
                .body(requestDTO.body())
                .attempts(0)
                .status(EmailRequestStatus.PENDING)
                .build();

        EmailRequest savedEmail = emailRequestRepository.save(emailRequest);

        try {
            emailEventProducer.publishEmailSendRequested(savedEmail.getEmailRequestId());

        } catch (EmailEventPublishException e) {
            savedEmail.setStatus(EmailRequestStatus.PUBLISH_FAILED);
            savedEmail.setErrorMessage(e.getMessage());
            emailRequestRepository.save(savedEmail);

            throw e;
        }

        return new SendEmailResponseDTO(savedEmail.getEmailRequestId(), savedEmail.getStatus());
    }


    public GetEmailResponseDTO getEmailRequest(UUID id) {
        EmailRequest emailRequest = emailRequestRepository.findById(id)
                .orElseThrow(EmailRequestNotFoundException::new);

        return new GetEmailResponseDTO(
                emailRequest.getEmailRequestId(),
                emailRequest.getRecipient(),
                emailRequest.getSubject(),
                emailRequest.getStatus(),
                emailRequest.getAttempts(),
                emailRequest.getErrorMessage(),
                emailRequest.getCreatedAt(),
                emailRequest.getUpdatedAt(),
                emailRequest.getSentAt()
        );
    }
}
