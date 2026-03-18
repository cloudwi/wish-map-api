CREATE TABLE friends (
    id          BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_friends_pair UNIQUE (requester_id, receiver_id),
    CONSTRAINT chk_no_self_friend CHECK (requester_id <> receiver_id)
);

CREATE INDEX idx_friends_requester ON friends(requester_id);
CREATE INDEX idx_friends_receiver  ON friends(receiver_id);
CREATE INDEX idx_friends_status    ON friends(status);
CREATE INDEX idx_users_nickname    ON users(nickname);
