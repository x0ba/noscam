ALTER TABLE accounts
    ADD COLUMN public_id UUID;

UPDATE accounts
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE accounts
    ALTER COLUMN public_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE accounts
    ADD CONSTRAINT accounts_public_id_unique UNIQUE (public_id);
