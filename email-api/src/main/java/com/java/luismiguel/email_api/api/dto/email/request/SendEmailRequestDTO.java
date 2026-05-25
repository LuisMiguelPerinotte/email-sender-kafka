package com.java.luismiguel.email_api.api.dto.email.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendEmailRequestDTO (
        @NotBlank(message = "Recipient is required")
        @Email(message = "Recipient must be a valid email")
        @Size(max = 255, message = "Recipient must have at most 255 characters")
        String recipient,

        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must have at most 255 characters")
        String subject,

        @NotBlank(message = "Body is required")
        @Size(max = 10000, message = "Body must have at most 10000 characters")
        String body
) {
}
