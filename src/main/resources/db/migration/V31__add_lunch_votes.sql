-- 점심 투표
CREATE TABLE lunch_votes (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL DEFAULT '점심 투표',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deadline TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 후보 장소
CREATE TABLE lunch_vote_candidates (
    id BIGSERIAL PRIMARY KEY,
    vote_id BIGINT NOT NULL REFERENCES lunch_votes(id) ON DELETE CASCADE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    added_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(vote_id, restaurant_id)
);

-- 투표 선택
CREATE TABLE lunch_vote_selections (
    id BIGSERIAL PRIMARY KEY,
    vote_id BIGINT NOT NULL REFERENCES lunch_votes(id) ON DELETE CASCADE,
    candidate_id BIGINT NOT NULL REFERENCES lunch_vote_candidates(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(vote_id, user_id)
);

-- 인덱스
CREATE INDEX idx_lunch_votes_group ON lunch_votes(group_id, status);
CREATE UNIQUE INDEX idx_one_active_vote_per_group ON lunch_votes(group_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_lunch_vote_candidates_vote ON lunch_vote_candidates(vote_id);
CREATE INDEX idx_lunch_vote_selections_vote ON lunch_vote_selections(vote_id);
