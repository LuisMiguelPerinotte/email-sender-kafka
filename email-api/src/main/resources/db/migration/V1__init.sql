-- Email Requests
CREATE TABLE email_requests(
    email_request_id UUID PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,

    CONSTRAINT chk_email_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED'))
);


--Update timestamp function --
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_email_requests_updated_at
    BEFORE UPDATE ON email_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();