package com.java.luismiguel.email_worker.infrastructure.kafka.consumer;

import com.java.luismiguel.email_worker.application.email.EmailSendingService;
import com.java.luismiguel.email_worker.infrastructure.kafka.event.EmailSendRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSendConsumer {
    private final EmailSendingService emailSendingService;

    @KafkaListener(
            topics = "email.send.requested",
            groupId = "email-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(EmailSendRequestedEvent event) {
        log.info("Email send requested received. emailRequestId={}", event.emailRequestId());
        emailSendingService.processEmailSending(event.emailRequestId());
    }
}
