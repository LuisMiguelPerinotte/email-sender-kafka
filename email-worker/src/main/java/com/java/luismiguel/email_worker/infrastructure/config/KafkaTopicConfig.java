package com.java.luismiguel.email_worker.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic emailSendRequestedDLT() {
        return new NewTopic("email.send.requested.dlt", 2, (short) 1);
    }
}
