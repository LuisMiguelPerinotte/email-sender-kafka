package com.java.luismiguel.email_api.api.dto.email.response;

import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;

import java.util.UUID;

public record RetryEmailSendResponseDTO(
        UUID emailId,
        EmailRequestStatus status
) {
}
