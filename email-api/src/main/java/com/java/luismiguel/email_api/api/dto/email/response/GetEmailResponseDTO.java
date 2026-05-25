package com.java.luismiguel.email_api.api.dto.email.response;

import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetEmailResponseDTO(
        UUID emailRequestId,
        String recipient,
        String subject,
        EmailRequestStatus status,
        Integer attempts,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime sentAt
) {
}
