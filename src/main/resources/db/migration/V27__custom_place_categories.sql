-- icon 컬럼 크기 확장 (Ionicons name 저장용)
ALTER TABLE place_categories ALTER COLUMN icon TYPE VARCHAR(30);

-- 직접 등록 전용 카테고리 구분을 위한 custom_only 플래그
ALTER TABLE place_categories ADD COLUMN custom_only BOOLEAN NOT NULL DEFAULT FALSE;

-- 기존 카테고리에 아이콘 설정 (Ionicons name)
UPDATE place_categories SET icon = 'restaurant' WHERE name = '음식점';
UPDATE place_categories SET icon = 'cafe' WHERE name = '카페,디저트';
UPDATE place_categories SET icon = 'cart' WHERE name = '쇼핑,유통';
UPDATE place_categories SET icon = 'storefront' WHERE name = '생활,편의';
UPDATE place_categories SET icon = 'bed' WHERE name = '여행,숙박';
UPDATE place_categories SET icon = 'color-palette' WHERE name = '문화,예술';
UPDATE place_categories SET icon = 'school' WHERE name = '교육,학문';
UPDATE place_categories SET icon = 'medkit' WHERE name = '의료,건강';

-- 직접 등록 전용 카테고리 추가
INSERT INTO place_categories (name, icon, priority, has_price_range, active, custom_only) VALUES
('붕어빵', 'fish', 100, TRUE, TRUE, TRUE),
('벚꽃', 'flower', 101, FALSE, TRUE, TRUE);
