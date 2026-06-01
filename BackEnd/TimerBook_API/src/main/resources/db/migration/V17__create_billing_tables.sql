CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_customer_id VARCHAR(255),
    provider_subscription_id VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    current_period_end TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_subscriptions_provider
        CHECK (provider IN ('STRIPE', 'MERCADO_PAGO')),
    CONSTRAINT chk_user_subscriptions_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'EXPIRED', 'TRIALING'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_subscriptions_provider_subscription_id
    ON user_subscriptions (provider_subscription_id);

CREATE INDEX IF NOT EXISTS ix_user_subscriptions_user_id
    ON user_subscriptions (user_id);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_payment_id VARCHAR(255) NOT NULL UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'BRL',
    status VARCHAR(30) NOT NULL,
    paid_at TIMESTAMP,
    raw_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_transactions_provider
        CHECK (provider IN ('STRIPE', 'MERCADO_PAGO')),
    CONSTRAINT chk_payment_transactions_type
        CHECK (transaction_type IN ('FIRST_PAYMENT', 'RENEWAL', 'REFUND', 'CANCELLATION', 'CHARGEBACK')),
    CONSTRAINT chk_payment_transactions_status
        CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED', 'CANCELED'))
);

CREATE INDEX IF NOT EXISTS ix_payment_transactions_user_id
    ON payment_transactions (user_id);

CREATE INDEX IF NOT EXISTS ix_payment_transactions_provider
    ON payment_transactions (provider);

CREATE TABLE IF NOT EXISTS webhook_events (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    payload_hash VARCHAR(255) NOT NULL,
    raw_payload TEXT NOT NULL,
    processed_at TIMESTAMP,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_webhook_events_provider
        CHECK (provider IN ('STRIPE', 'MERCADO_PAGO')),
    CONSTRAINT chk_webhook_events_processing_status
        CHECK (processing_status IN ('RECEIVED', 'PROCESSED', 'IGNORED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS ix_webhook_events_provider
    ON webhook_events (provider);

CREATE INDEX IF NOT EXISTS ix_webhook_events_processing_status
    ON webhook_events (processing_status);
