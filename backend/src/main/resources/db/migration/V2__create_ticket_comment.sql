CREATE TABLE ticket_comment (
    id         BIGSERIAL PRIMARY KEY,
    ticket_id  BIGINT       NOT NULL,
    author     VARCHAR(100) NOT NULL,
    body       TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_ticket_comment_ticket
        FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE RESTRICT
);

CREATE INDEX idx_ticket_comment_ticket_created ON ticket_comment (ticket_id, created_at);
