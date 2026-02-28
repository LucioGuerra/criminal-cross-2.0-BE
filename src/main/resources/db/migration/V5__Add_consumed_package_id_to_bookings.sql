ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS consumed_package_id BIGINT;
