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
    private UUID EmailRequestId;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private EmailRequestStatus status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
