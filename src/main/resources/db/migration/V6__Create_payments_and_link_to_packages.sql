CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    method VARCHAR(24) NOT NULL CHECK (method IN ('CASH', 'CARD', 'TRANSFER', 'OTHER')),
    paid_at DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_payments_paid_at ON payments(paid_at);
CREATE INDEX IF NOT EXISTS idx_payments_method ON payments(method);

ALTER TABLE client_packages
    ADD COLUMN IF NOT EXISTS payment_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_client_packages_payment_id ON client_packages(payment_id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_client_packages_payment') THEN
        ALTER TABLE client_packages
            ADD CONSTRAINT fk_client_packages_payment
                FOREIGN KEY (payment_id)
                    REFERENCES payments(id)
                    ON DELETE RESTRICT;
    END IF;
END
$$;
