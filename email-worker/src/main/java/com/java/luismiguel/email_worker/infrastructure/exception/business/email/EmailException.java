package com.java.luismiguel.email_worker.infrastructure.exception.business.email;

import com.java.luismiguel.email_worker.infrastructure.exception.business.BusinessException;

public class EmailException extends BusinessException {
    public EmailException(String message) {
        super(message);
    }
}
