package com.java.luismiguel.email_api.infrastructure.exception.business.email;

import com.java.luismiguel.email_api.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailException extends BusinessException {
    public EmailException(String message, HttpStatus status) {
        super(message, status);
    }
}
