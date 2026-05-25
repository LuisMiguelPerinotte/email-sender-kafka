package com.java.luismiguel.email_api.domain.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailRequestRepository extends JpaRepository<EmailRequest, UUID> {
}
