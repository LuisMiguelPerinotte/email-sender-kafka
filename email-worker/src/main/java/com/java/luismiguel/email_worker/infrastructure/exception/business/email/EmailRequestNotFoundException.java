package com.java.luismiguel.email_worker.infrastructure.exception.business.email;

public class EmailRequestNotFoundException extends EmailException {
    public EmailRequestNotFoundException() {
        super("Email Request Not Found!");
    }
}
