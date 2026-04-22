-- 트렌드 태그 재구성 + place_category_tags 일부 정리.
-- Why: 기존 trend_tags는 시즌성(벚꽃, 붕어빵)·너무 특화된 상품명(두쫀쿠, 버터떡)이 섞여
--      일반 사용자에게 유용성 낮음. 카테고리 + 태그 조합으로 실제 검색 필터로 쓰이는 구조로 재편.

-- 1) 기존 trend_tags 전체 제거
DELETE FROM trend_tags;

-- 2) 새 트렌드 태그: 각 행이 (카테고리, 태그, 가격대) 중 하나 이상을 결합해 검색 필터 조건으로 기능
--    앱이 /api/v1/trend-tags에서 placeCategoryId + tags + priceRange를 받아 그대로 필터 적용.
INSERT INTO trend_tags (label, category_name, tags, price_range, priority, active) VALUES
    ('💰 거지맵',        '음식점',      NULL,           'UNDER_10K', 1, TRUE),
    ('🍚 혼밥 성지',     '음식점',      '혼밥 성지',      NULL,        2, TRUE),
    ('🍻 회식 맛집',     '음식점',      '회식 추천',      NULL,        3, TRUE),
    ('💕 데이트',        '음식점',      '데이트',        NULL,        4, TRUE),
    ('🥇 가성비',        '음식점',      '가성비 갑',      NULL,        5, TRUE),
    ('☕ 조용한 카페',    '카페,디저트', '조용한',         NULL,        6, TRUE),
    ('💻 작업하기 좋은', '카페,디저트', '작업하기 좋은',   NULL,        7, TRUE),
    ('🍰 디저트 맛집',    '카페,디저트', '디저트 맛집',     NULL,        8, TRUE);

-- 3) place_category_tags 정리: 의미가 모호해 실제로 잘 안 쓰일 태그 제거.
--    음식점 맛 특징 "짜릿한"은 구체적인 맛 표현이 아니라 선택 가이드로 애매 → 제거.
DELETE FROM place_category_tags
WHERE tag_group = '맛 특징' AND tag = '짜릿한';
