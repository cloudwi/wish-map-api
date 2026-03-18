-- 1. 좋아요 그룹 테이블
CREATE TABLE like_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, name)
);
CREATE INDEX idx_like_groups_user ON like_groups(user_id);

-- 2. 기존 좋아요 → 기본 그룹으로 마이그레이션
INSERT INTO like_groups (user_id, name)
SELECT DISTINCT user_id, '기본' FROM likes;

ALTER TABLE likes ADD COLUMN like_group_id BIGINT REFERENCES like_groups(id) ON DELETE CASCADE;

UPDATE likes SET like_group_id = lg.id
FROM like_groups lg WHERE likes.user_id = lg.user_id AND lg.name = '기본';

ALTER TABLE likes ALTER COLUMN like_group_id SET NOT NULL;
ALTER TABLE likes DROP CONSTRAINT likes_restaurant_id_user_id_key;
ALTER TABLE likes ADD CONSTRAINT uk_likes_group_restaurant UNIQUE (like_group_id, restaurant_id);

-- 3. 북마크 테이블 삭제
DROP TABLE bookmarks;

-- 4. 방문 UNIQUE 제약 제거 + 날짜 기반 인덱스
ALTER TABLE visits DROP CONSTRAINT uk_visits;
CREATE INDEX idx_visits_restaurant_user_date ON visits(restaurant_id, user_id, created_at);

-- 5. 주소 컬럼 제거
ALTER TABLE restaurants DROP COLUMN address;
