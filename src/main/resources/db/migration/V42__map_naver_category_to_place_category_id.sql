-- 네이버 카테고리 원문(places.category) → 앱 자체 카테고리(place_category_id) 소급 매핑.
-- Why: 이전엔 등록 시 자동 매핑 로직이 없어 place_category_id=NULL로 쌓여 카테고리별 태그가 노출되지 않는 문제.
--      Kotlin PlaceService.resolvePlaceCategoryId()와 동일한 규칙을 SQL로 1회 소급 적용.
--      네이버 원문 places.category는 그대로 보존 (UPDATE되지 않음).
--      id는 하드코딩하지 않고 place_categories.name 기준으로 JOIN해 조회 (순서/id 변경에 안전).

-- 안전장치: 롤백용 백업 스냅샷
CREATE TABLE IF NOT EXISTS _backup_places_v42 AS
SELECT id, category, place_category_id FROM places;

-- 1) 음식점 - exact match
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '음식점'
  AND p.category IS NOT NULL
  AND TRIM(SPLIT_PART(p.category, '>', 1)) IN (
    '한식', '중식', '양식', '일식', '분식', '아시아음식', '퓨전요리',
    '뷔페', '치킨', '패스트푸드', '술집', '도시락', '간식'
  );

-- 2) 음식점 - 포괄 패턴 (육류,고기요리 등)
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '음식점'
  AND p.category IS NOT NULL
  AND TRIM(SPLIT_PART(p.category, '>', 1)) ~ '(요리|고기|뷔페|치킨|피자|면|국|탕|찌개|구이)';

-- 3) 카페,디저트
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '카페,디저트'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%카페%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%디저트%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%빵%'
  );

-- 4) 쇼핑,유통
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '쇼핑,유통'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) IN ('마트', '백화점', '의류', '신발')
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%쇼핑%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%잡화%'
  );

-- 5) 생활,편의
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '생활,편의'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%편의%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%세탁%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%미용%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%부동산%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%철물%'
  );

-- 6) 여행,숙박
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '여행,숙박'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%숙박%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%호텔%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%펜션%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%모텔%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%관광%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%리조트%'
  );

-- 7) 문화,예술
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '문화,예술'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%영화%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%박물관%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%공연%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%미술%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%전시%'
  );

-- 8) 교육,학문
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '교육,학문'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%학원%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%학교%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%도서%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%교육%'
  );

-- 9) 의료,건강
UPDATE places p SET place_category_id = pc.id
FROM place_categories pc
WHERE p.place_category_id IS NULL
  AND pc.name = '의료,건강'
  AND p.category IS NOT NULL
  AND (
    TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%병원%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%의원%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%약국%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%치과%'
    OR TRIM(SPLIT_PART(p.category, '>', 1)) LIKE '%한의원%'
  );

-- 10) 잔여: 매핑 실패 건은 null 그대로 유지.
--     Kotlin 로직에서도 null로 남고, 배포 후 로그(WARN "네이버 카테고리 매핑 실패")로 모니터링하여 후속 마이그레이션 보강.
