-- 기존 place_categories 데이터 교체 (네이버 카테고리 대분류 기반)
DELETE FROM place_category_tags;
DELETE FROM place_categories;

INSERT INTO place_categories (id, name, icon, priority, has_price_range) VALUES
(1, '음식점', NULL, 1, TRUE),
(2, '카페,디저트', NULL, 2, TRUE),
(3, '쇼핑,유통', NULL, 3, FALSE),
(4, '생활,편의', NULL, 4, FALSE),
(5, '여행,숙박', NULL, 5, FALSE),
(6, '문화,예술', NULL, 6, FALSE),
(7, '교육,학문', NULL, 7, FALSE),
(8, '의료,건강', NULL, 8, FALSE);

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

-- 문화,예술 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(6, '특징', '볼거리 많은', 1), (6, '특징', '조용한', 2), (6, '특징', '가족 추천', 3), (6, '특징', '데이트', 4);

-- 의료,건강 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(7, '특징', '친절한', 1), (7, '특징', '실력 좋은', 2), (7, '특징', '대기 없음', 3), (7, '특징', '깨끗한', 4);

-- 교육,학문 태그
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(8, '특징', '전문적인', 1), (8, '특징', '친절한', 2), (8, '특징', '가성비', 3);

-- 시퀀스 리셋
SELECT setval('place_categories_id_seq', 8);
SELECT setval('place_category_tags_id_seq', (SELECT MAX(id) FROM place_category_tags));
