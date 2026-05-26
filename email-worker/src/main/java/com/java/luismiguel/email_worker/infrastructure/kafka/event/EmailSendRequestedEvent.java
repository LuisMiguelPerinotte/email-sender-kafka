package com.java.luismiguel.email_worker.infrastructure.kafka.event;

import java.util.UUID;

public record EmailSendRequestedEvent(
        UUID emailRequestId
) {
}
