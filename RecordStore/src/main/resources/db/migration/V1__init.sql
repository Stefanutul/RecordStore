-- V1__init.sql

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- USER ROLES (ElementCollection<Role>)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- RECORDS
CREATE TABLE IF NOT EXISTS records (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    artist VARCHAR(255),
    label VARCHAR(255),
    genre VARCHAR(255),
    price NUMERIC(19,2),
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_records_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- TRACKS
CREATE TABLE IF NOT EXISTS tracks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration_seconds INTEGER,
    track_key VARCHAR(255) NOT NULL,
    record_id BIGINT NOT NULL,
    CONSTRAINT fk_tracks_record
        FOREIGN KEY (record_id) REFERENCES records(id)
);

-- Indexes (optional but useful)
CREATE INDEX IF NOT EXISTS idx_records_owner_id ON records(owner_id);
CREATE INDEX IF NOT EXISTS idx_tracks_record_id ON tracks(record_id);
