CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    method VARCHAR(24) NOT NULL,
    paid_at DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_payments_paid_at ON payments(paid_at);
CREATE INDEX idx_payments_method ON payments(method);

ALTER TABLE client_packages
    ADD COLUMN IF NOT EXISTS payment_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_client_packages_payment_id ON client_packages(payment_id);

ALTER TABLE client_packages
    ADD CONSTRAINT fk_client_packages_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments(id)
            ON DELETE RESTRICT;
