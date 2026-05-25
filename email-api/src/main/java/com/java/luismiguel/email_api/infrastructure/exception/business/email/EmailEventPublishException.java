package com.java.luismiguel.email_api.infrastructure.exception.business.email;

import org.springframework.http.HttpStatus;

public class EmailEventPublishException extends EmailException {
    public EmailEventPublishException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}
