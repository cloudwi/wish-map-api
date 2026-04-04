-- 장소 카테고리 테이블
CREATE TABLE place_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(10),
    priority INT NOT NULL DEFAULT 0,
    has_price_range BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 카테고리별 태그 테이블
CREATE TABLE place_category_tags (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES place_categories(id),
    tag_group VARCHAR(50) NOT NULL,
    tag VARCHAR(50) NOT NULL,
    priority INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_place_category_tags_category ON place_category_tags(category_id);

-- 초기 카테고리 시드
INSERT INTO place_categories (name, icon, priority, has_price_range) VALUES
('음식', '🍽', 1, TRUE),
('카페', '☕', 2, FALSE),
('디저트/간식', '🍞', 3, FALSE),
('자연/풍경', '🌸', 4, FALSE),
('생활편의', '🔧', 5, FALSE);

-- 음식 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(1, '분위기', '혼밥 성지', 1), (1, '분위기', '회식 추천', 2), (1, '분위기', '데이트', 3), (1, '분위기', '조용한', 4), (1, '분위기', '활기찬', 5),
(1, '맛 특징', '매운맛', 1), (1, '맛 특징', '달콤한', 2), (1, '맛 특징', '담백한', 3), (1, '맛 특징', '짜릿한', 4), (1, '맛 특징', '고소한', 5),
(1, '편의', '주차 편해', 1), (1, '편의', '대기 없음', 2), (1, '편의', '늦게까지', 3), (1, '편의', '반려동물 OK', 4),
(1, '한줄평', '또 갈 집', 1), (1, '한줄평', '숨은 맛집', 2), (1, '한줄평', '점심 맛집', 3), (1, '한줄평', '가성비 갑', 4);

-- 카페 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(2, '분위기', '조용한', 1), (2, '분위기', '넓은', 2), (2, '분위기', '루프탑', 3), (2, '분위기', '감성적인', 4), (2, '분위기', '작업하기 좋은', 5),
(2, '메뉴', '커피 맛집', 1), (2, '메뉴', '디저트 맛집', 2), (2, '메뉴', '브런치', 3), (2, '메뉴', '음료 다양', 4),
(2, '한줄평', '또 갈 곳', 1), (2, '한줄평', '숨은 카페', 2), (2, '한줄평', '뷰 맛집', 3);

-- 디저트/간식 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(3, '종류', '붕어빵', 1), (3, '종류', '호떡', 2), (3, '종류', '타코야끼', 3), (3, '종류', '와플', 4), (3, '종류', '마카롱', 5),
(3, '특징', '줄 서는 곳', 1), (3, '특징', '가성비', 2), (3, '특징', '수제', 3), (3, '특징', '계절 한정', 4);

-- 자연/풍경 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(4, '종류', '벚꽃', 1), (4, '종류', '단풍', 2), (4, '종류', '야경', 3), (4, '종류', '일출', 4), (4, '종류', '공원', 5),
(4, '특징', '사진 맛집', 1), (4, '특징', '산책 코스', 2), (4, '특징', '드라이브', 3), (4, '특징', '피크닉', 4);

-- 생활편의 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(5, '종류', '철물점', 1), (5, '종류', '세탁소', 2), (5, '종류', '수선집', 3), (5, '종류', '열쇠', 4),
(5, '특징', '친절한', 1), (5, '특징', '가성비', 2), (5, '특징', '실력 좋은', 3), (5, '특징', '빠른', 4);

-- restaurants.price_range nullable로 변경
ALTER TABLE restaurants ALTER COLUMN price_range DROP NOT NULL;

-- restaurants에 place_category_id 추가
ALTER TABLE restaurants ADD COLUMN place_category_id BIGINT REFERENCES place_categories(id);

-- 기존 데이터는 '음식' 카테고리로 설정
UPDATE restaurants SET place_category_id = 1;

-- visits.price_range nullable로 변경
ALTER TABLE visits ALTER COLUMN price_range DROP NOT NULL;

-- place_category_id 필터링용 인덱스
CREATE INDEX idx_restaurants_place_category ON restaurants(place_category_id);
