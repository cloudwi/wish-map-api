-- 기존 카테고리 이름을 네이버 대분류 기반으로 변경
UPDATE place_categories SET name = '음식점' WHERE id = 1;
UPDATE place_categories SET name = '카페,디저트', has_price_range = TRUE WHERE id = 2;
UPDATE place_categories SET name = '쇼핑,유통' WHERE id = 3;
UPDATE place_categories SET name = '생활,편의' WHERE id = 4;
UPDATE place_categories SET name = '여행,숙박' WHERE id = 5;

-- 새 카테고리 추가
INSERT INTO place_categories (name, icon, priority, has_price_range) VALUES
('문화,예술', NULL, 6, FALSE),
('교육,학문', NULL, 7, FALSE),
('의료,건강', NULL, 8, FALSE);

-- 기존 태그 삭제 후 재생성
DELETE FROM place_category_tags;

-- 음식점 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(1, '분위기', '혼밥 성지', 1), (1, '분위기', '회식 추천', 2), (1, '분위기', '데이트', 3), (1, '분위기', '조용한', 4), (1, '분위기', '활기찬', 5),
(1, '맛 특징', '매운맛', 1), (1, '맛 특징', '달콤한', 2), (1, '맛 특징', '담백한', 3), (1, '맛 특징', '짜릿한', 4), (1, '맛 특징', '고소한', 5),
(1, '편의', '주차 편해', 1), (1, '편의', '대기 없음', 2), (1, '편의', '늦게까지', 3), (1, '편의', '반려동물 OK', 4),
(1, '한줄평', '또 갈 집', 1), (1, '한줄평', '숨은 맛집', 2), (1, '한줄평', '점심 맛집', 3), (1, '한줄평', '가성비 갑', 4);

-- 카페,디저트 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(2, '분위기', '조용한', 1), (2, '분위기', '넓은', 2), (2, '분위기', '루프탑', 3), (2, '분위기', '감성적인', 4), (2, '분위기', '작업하기 좋은', 5),
(2, '메뉴', '커피 맛집', 1), (2, '메뉴', '디저트 맛집', 2), (2, '메뉴', '브런치', 3), (2, '메뉴', '음료 다양', 4),
(2, '한줄평', '또 갈 곳', 1), (2, '한줄평', '숨은 카페', 2), (2, '한줄평', '뷰 맛집', 3);

-- 쇼핑,유통 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(3, '특징', '가성비', 1), (3, '특징', '품질 좋은', 2), (3, '특징', '종류 다양', 3), (3, '특징', '친절한', 4);

-- 생활,편의 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(4, '종류', '철물점', 1), (4, '종류', '세탁소', 2), (4, '종류', '수선집', 3), (4, '종류', '열쇠', 4),
(4, '특징', '친절한', 1), (4, '특징', '가성비', 2), (4, '특징', '실력 좋은', 3), (4, '특징', '빠른', 4);

-- 여행,숙박 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(5, '특징', '뷰 맛집', 1), (5, '특징', '깨끗한', 2), (5, '특징', '가성비', 3), (5, '특징', '위치 좋은', 4);

-- 문화,예술 태그 (새 카테고리 ID 조회)
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '볼거리 많은', 1 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '특징', '조용한', 2 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '특징', '가족 추천', 3 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '특징', '데이트', 4 FROM place_categories WHERE name = '문화,예술';

-- 교육,학문 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '전문적인', 1 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '특징', '친절한', 2 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '특징', '가성비', 3 FROM place_categories WHERE name = '교육,학문';

-- 의료,건강 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '친절한', 1 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '특징', '실력 좋은', 2 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '특징', '대기 없음', 3 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '특징', '깨끗한', 4 FROM place_categories WHERE name = '의료,건강';
