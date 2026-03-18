-- ============================================
-- V8: 누락 테이블 전체 생성 + address 수정
-- 프로덕션 DB에 V1~V7이 Flyway로 적용되지 않았으므로
-- 현재 DB 상태 기준으로 누락된 것들을 모두 추가
-- ============================================

-- 1. address 컬럼 nullable로 변경
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'restaurants' AND column_name = 'address'
    ) THEN
        ALTER TABLE restaurants ALTER COLUMN address DROP NOT NULL;
    END IF;
END $$;

-- 2. visits 테이블 (V4에서 추가되어야 했음)
CREATE TABLE IF NOT EXISTS visits (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_visits_restaurant ON visits(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_visits_user ON visits(user_id);
CREATE INDEX IF NOT EXISTS idx_visits_restaurant_user_date ON visits(restaurant_id, user_id, created_at);

-- 3. like_groups 테이블 (V5에서 추가되어야 했음)
CREATE TABLE IF NOT EXISTS like_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, name)
);
CREATE INDEX IF NOT EXISTS idx_like_groups_user ON like_groups(user_id);

-- 4. likes에 like_group_id 컬럼 추가 (없는 경우만)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'likes' AND column_name = 'like_group_id'
    ) THEN
        ALTER TABLE likes ADD COLUMN like_group_id BIGINT REFERENCES like_groups(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 5. friends 테이블 (V6에서 추가되어야 했음)
CREATE TABLE IF NOT EXISTS friends (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (requester_id, receiver_id)
);
CREATE INDEX IF NOT EXISTS idx_friends_requester ON friends(requester_id);
CREATE INDEX IF NOT EXISTS idx_friends_receiver ON friends(receiver_id);

-- 6. 그룹 테이블
CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    leader_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_groups_leader ON groups(leader_id);

-- 7. 그룹 멤버 테이블
CREATE TABLE IF NOT EXISTS group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (group_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_group_members_group ON group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members(user_id);
