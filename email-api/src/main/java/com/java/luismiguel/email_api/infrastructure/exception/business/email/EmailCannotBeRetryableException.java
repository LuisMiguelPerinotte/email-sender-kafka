package com.java.luismiguel.email_api.infrastructure.exception.business.email;

import org.springframework.http.HttpStatus;

public class EmailCannotBeRetryableException extends EmailException {
    public EmailCannotBeRetryableException() {
        super("E-mail Cannot Be Retryable!", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
