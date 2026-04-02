-- comment_tags 테이블 생성
CREATE TABLE comment_tags (
    id            BIGSERIAL PRIMARY KEY,
    comment_id    BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    tag           VARCHAR(50) NOT NULL,
    category      VARCHAR(30)
);

CREATE INDEX idx_comment_tags_comment_id ON comment_tags(comment_id);
CREATE INDEX idx_comment_tags_tag ON comment_tags(tag);

-- 기존 content에서 태그 추출하여 마이그레이션
-- atmosphere
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '혼밥 성지', 'atmosphere' FROM comments WHERE content LIKE '%#혼밥 성지%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '회식 추천', 'atmosphere' FROM comments WHERE content LIKE '%#회식 추천%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '데이트', 'atmosphere' FROM comments WHERE content LIKE '%#데이트%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '조용한', 'atmosphere' FROM comments WHERE content LIKE '%#조용한%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '활기찬', 'atmosphere' FROM comments WHERE content LIKE '%#활기찬%' AND is_deleted = false;

-- taste
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '매운맛', 'taste' FROM comments WHERE content LIKE '%#매운맛%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '달콤한', 'taste' FROM comments WHERE content LIKE '%#달콤한%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '담백한', 'taste' FROM comments WHERE content LIKE '%#담백한%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '짜릿한', 'taste' FROM comments WHERE content LIKE '%#짜릿한%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '고소한', 'taste' FROM comments WHERE content LIKE '%#고소한%' AND is_deleted = false;

-- convenience
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '주차 편해', 'convenience' FROM comments WHERE content LIKE '%#주차 편해%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '대기 없음', 'convenience' FROM comments WHERE content LIKE '%#대기 없음%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '늦게까지', 'convenience' FROM comments WHERE content LIKE '%#늦게까지%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '반려동물 OK', 'convenience' FROM comments WHERE content LIKE '%#반려동물 OK%' AND is_deleted = false;

-- oneLiner
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '또 갈 집', 'oneLiner' FROM comments WHERE content LIKE '%#또 갈 집%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '숨은 맛집', 'oneLiner' FROM comments WHERE content LIKE '%#숨은 맛집%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '점심 맛집', 'oneLiner' FROM comments WHERE content LIKE '%#점심 맛집%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '줄 서는 집', 'oneLiner' FROM comments WHERE content LIKE '%#줄 서는 집%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '가성비 갑', 'oneLiner' FROM comments WHERE content LIKE '%#가성비 갑%' AND is_deleted = false;
INSERT INTO comment_tags (comment_id, tag, category)
SELECT id, '뷰 맛집', 'oneLiner' FROM comments WHERE content LIKE '%#뷰 맛집%' AND is_deleted = false;

-- content에서 태그 제거 (긴 태그부터 처리하여 부분 매칭 방지)
UPDATE comments SET content = REPLACE(content, '#반려동물 OK', '') WHERE content LIKE '%#반려동물 OK%';
UPDATE comments SET content = REPLACE(content, '#혼밥 성지', '') WHERE content LIKE '%#혼밥 성지%';
UPDATE comments SET content = REPLACE(content, '#회식 추천', '') WHERE content LIKE '%#회식 추천%';
UPDATE comments SET content = REPLACE(content, '#주차 편해', '') WHERE content LIKE '%#주차 편해%';
UPDATE comments SET content = REPLACE(content, '#대기 없음', '') WHERE content LIKE '%#대기 없음%';
UPDATE comments SET content = REPLACE(content, '#줄 서는 집', '') WHERE content LIKE '%#줄 서는 집%';
UPDATE comments SET content = REPLACE(content, '#또 갈 집', '') WHERE content LIKE '%#또 갈 집%';
UPDATE comments SET content = REPLACE(content, '#숨은 맛집', '') WHERE content LIKE '%#숨은 맛집%';
UPDATE comments SET content = REPLACE(content, '#점심 맛집', '') WHERE content LIKE '%#점심 맛집%';
UPDATE comments SET content = REPLACE(content, '#가성비 갑', '') WHERE content LIKE '%#가성비 갑%';
UPDATE comments SET content = REPLACE(content, '#뷰 맛집', '') WHERE content LIKE '%#뷰 맛집%';
UPDATE comments SET content = REPLACE(content, '#데이트', '') WHERE content LIKE '%#데이트%';
UPDATE comments SET content = REPLACE(content, '#조용한', '') WHERE content LIKE '%#조용한%';
UPDATE comments SET content = REPLACE(content, '#활기찬', '') WHERE content LIKE '%#활기찬%';
UPDATE comments SET content = REPLACE(content, '#매운맛', '') WHERE content LIKE '%#매운맛%';
UPDATE comments SET content = REPLACE(content, '#달콤한', '') WHERE content LIKE '%#달콤한%';
UPDATE comments SET content = REPLACE(content, '#담백한', '') WHERE content LIKE '%#담백한%';
UPDATE comments SET content = REPLACE(content, '#짜릿한', '') WHERE content LIKE '%#짜릿한%';
UPDATE comments SET content = REPLACE(content, '#고소한', '') WHERE content LIKE '%#고소한%';
UPDATE comments SET content = REPLACE(content, '#늦게까지', '') WHERE content LIKE '%#늦게까지%';

-- 남은 공백/개행 정리
UPDATE comments SET content = TRIM(content) WHERE content != TRIM(content);
