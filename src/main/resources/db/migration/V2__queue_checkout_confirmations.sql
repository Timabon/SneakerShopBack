CREATE TABLE checkout_confirmations (
    order_id bigint PRIMARY KEY REFERENCES checkout_orders(order_id),
    recipient text NOT NULL,
    deadline timestamptz NOT NULL DEFAULT now() + interval '23 hours',
    next_attempt_at timestamptz NOT NULL DEFAULT now(),
    attempts integer NOT NULL DEFAULT 0,
    sent_at timestamptz
);

CREATE INDEX checkout_confirmations_pending_idx ON checkout_confirmations (next_attempt_at)
    WHERE sent_at IS NULL;
