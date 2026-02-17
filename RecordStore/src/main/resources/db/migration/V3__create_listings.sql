CREATE TABLE IF NOT EXISTS listings (
    id BIGSERIAL PRIMARY KEY,

    record_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,

    price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,

    status VARCHAR(16) NOT NULL,

    reserved_by BIGINT,
    reserved_until TIMESTAMPTZ,

    buyer_id BIGINT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    version BIGINT,

    CONSTRAINT fk_listings_record
        FOREIGN KEY (record_id) REFERENCES records(id)
);

CREATE INDEX IF NOT EXISTS idx_listing_status ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listing_record_id ON listings(record_id);
