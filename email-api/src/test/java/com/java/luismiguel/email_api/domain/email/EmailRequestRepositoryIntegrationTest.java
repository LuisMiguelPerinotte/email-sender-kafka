package com.java.luismiguel.email_api.domain.email;

import com.java.luismiguel.email_api.domain.email.enums.EmailRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class EmailRequestRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    EmailRequestRepository emailRequestRepository;

    @Test
    @DisplayName("Should Save Email Request Successfully")
    void shouldSaveEmailRequestSuccessfully() {
        EmailRequest email = EmailRequest.builder()
                .recipient("user@email.com")
                .subject("Hello")
                .body("Testing")
                .status(EmailRequestStatus.PENDING)
                .build();

        EmailRequest savedEmail = emailRequestRepository.save(email);

        Optional<EmailRequest> result =  emailRequestRepository.findById(savedEmail.getEmailRequestId());

        assertThat(result).isPresent();

        assertThat(savedEmail.getEmailRequestId()).isNotNull();
        assertThat(result.get().getRecipient()).isEqualTo("user@email.com");
        assertThat(result.get().getSubject()).isEqualTo("Hello");
        assertThat(result.get().getBody()).isEqualTo("Testing");
        assertThat(result.get().getStatus()).isEqualTo(EmailRequestStatus.PENDING);
    }


    @Test
    @DisplayName("Should Return Empty When Email Request Does Not Exist")
    void shouldReturnEmptyWhenEmailRequestDoesNotExist() {
        Optional<EmailRequest> result =  emailRequestRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}

