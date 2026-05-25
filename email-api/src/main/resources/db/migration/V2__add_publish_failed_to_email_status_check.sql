ALTER TABLE email_requests
DROP CONSTRAINT chk_email_status;

ALTER TABLE email_requests
    ADD CONSTRAINT chk_email_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'PUBLISH_FAILED'));