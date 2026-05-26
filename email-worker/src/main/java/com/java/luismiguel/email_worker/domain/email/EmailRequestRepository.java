package com.java.luismiguel.email_worker.domain.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailRequestRepository extends JpaRepository<EmailRequest, UUID> {
}
