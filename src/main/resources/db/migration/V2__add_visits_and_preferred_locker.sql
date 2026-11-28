CREATE TABLE visits (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    membership_id BIGINT NOT NULL REFERENCES memberships(id) ON DELETE CASCADE,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    locker_number VARCHAR(20)
);

ALTER TABLE clients ADD COLUMN preferred_locker VARCHAR(20);
