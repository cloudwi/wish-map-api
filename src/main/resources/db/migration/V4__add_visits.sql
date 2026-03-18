CREATE TABLE visits (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_visits UNIQUE (restaurant_id, user_id)
);

CREATE INDEX idx_visits_restaurant ON visits(restaurant_id);
CREATE INDEX idx_visits_user ON visits(user_id);
