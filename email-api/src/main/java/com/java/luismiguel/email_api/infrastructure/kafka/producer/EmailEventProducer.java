package com.java.luismiguel.email_api.infrastructure.kafka.producer;

import com.java.luismiguel.email_api.infrastructure.exception.business.email.EmailEventPublishException;
import com.java.luismiguel.email_api.infrastructure.kafka.event.EmailSendRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class EmailEventProducer {
    private static final String TOPIC = "email.send.requested";

    private final KafkaTemplate<String, EmailSendRequestedEvent> kafkaTemplate;

    public void publishEmailSendRequested(UUID emailRequestId) {
        EmailSendRequestedEvent event = new EmailSendRequestedEvent(emailRequestId);

        try {
            kafkaTemplate.send(TOPIC, emailRequestId.toString(), event).get(3, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailEventPublishException("Thread interrupted while publishing email event", e);

        } catch (ExecutionException e) {
            throw new EmailEventPublishException("Failed to publish email event to Kafka", e);

        } catch (TimeoutException e) {
            throw new EmailEventPublishException("Timeout while publishing email event", e);
        }
    }
}
