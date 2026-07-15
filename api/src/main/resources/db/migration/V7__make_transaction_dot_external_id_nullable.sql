ALTER TABLE transactions
    ALTER COLUMN external_id DROP NOT NULL;

ALTER TABLE transactions
    ADD CONSTRAINT uq_transactions_external_source
        UNIQUE (account_id, source_type, external_id);