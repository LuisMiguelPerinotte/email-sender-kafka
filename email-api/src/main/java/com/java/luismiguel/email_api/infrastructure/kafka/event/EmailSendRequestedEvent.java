package com.java.luismiguel.email_api.infrastructure.kafka.event;

import java.util.UUID;

public record EmailSendRequestedEvent(
        UUID emailRequestId
) {
}
