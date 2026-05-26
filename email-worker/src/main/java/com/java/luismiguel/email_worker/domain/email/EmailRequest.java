package com.java.luismiguel.email_worker.domain.email;

import com.java.luismiguel.email_worker.domain.email.enums.EmailRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_requests")
public class EmailRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_request_id")
    UUID EmailRequestId;

    @Column(name = "recipient", nullable = false)
    String recipient;

    @Column(name = "subject", nullable = false)
    String subject;

    @Column(name = "body", nullable = false)
    String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    EmailRequestStatus status;

    @Column(name = "attempts", nullable = false)
    Integer attempts;

    @Column(name = "error_message")
    String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "sent_at")
    LocalDateTime sentAt;
}
