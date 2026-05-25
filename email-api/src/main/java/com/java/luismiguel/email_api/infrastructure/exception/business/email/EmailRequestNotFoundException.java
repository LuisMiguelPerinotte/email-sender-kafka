package com.java.luismiguel.email_api.infrastructure.exception.business.email;

import org.springframework.http.HttpStatus;

public class EmailRequestNotFoundException extends EmailException {
    public EmailRequestNotFoundException() {
        super("Email Request Not Found!", HttpStatus.NOT_FOUND);
    }
}
