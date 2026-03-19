CREATE TABLE client_packages (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_client_packages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE client_package_credits (
    id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    tokens INTEGER NOT NULL,
    CONSTRAINT fk_client_package_credits_package FOREIGN KEY (package_id) REFERENCES client_packages(id) ON DELETE CASCADE,
    CONSTRAINT uq_client_package_activity UNIQUE (package_id, activity_id)
);

CREATE INDEX idx_client_packages_user_active ON client_packages(user_id, active);
CREATE UNIQUE INDEX uq_client_packages_user_active_true ON client_packages(user_id) WHERE active = true;
CREATE INDEX idx_client_packages_period_end ON client_packages(period_end);
CREATE INDEX idx_client_package_credits_package ON client_package_credits(package_id);
CREATE INDEX idx_client_package_credits_activity ON client_package_credits(activity_id);
