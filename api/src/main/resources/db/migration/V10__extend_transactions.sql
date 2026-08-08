-- Amounts are signed. Negative values are money leaving the account (purchases,
-- wires, fees). Positive values are money entering the account (refunds, deposits).
ALTER TABLE transactions
    ADD COLUMN merchant VARCHAR(255),
    ADD COLUMN display_name VARCHAR(255),
    ADD COLUMN original_description TEXT,
    ADD COLUMN authorized_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN posted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN pending BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN pending_transaction_id VARCHAR(128),
    ADD COLUMN category VARCHAR(255),
    ADD COLUMN payment_channel VARCHAR(64),
    ADD COLUMN iso_currency_country VARCHAR(2),
    ADD COLUMN merchant_country VARCHAR(2),
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN removed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN content_hash VARCHAR(64);

CREATE INDEX idx_transactions_user_posted_at ON transactions (user_id, posted_at DESC);
CREATE INDEX idx_transactions_account_posted_at ON transactions (account_id, posted_at DESC);
CREATE INDEX idx_transactions_user_status ON transactions (user_id, status);
