-- 네이버 지역검색 카테고리 → 앱 자체 카테고리 매핑 테이블.
-- Why: 이전 구조(Kotlin 하드코딩 Map)는 매핑 추가 시 매번 코드 배포 필요.
--      테이블로 옮겨 SQL INSERT만으로 운영 중 매핑 보강 가능하게 전환.
--      place_category_id는 하드코딩하지 않고 place_categories.name JOIN으로 조회.

CREATE TABLE naver_category_mapping (
    id BIGSERIAL PRIMARY KEY,
    naver_top VARCHAR(100) UNIQUE NOT NULL,
    place_category_id BIGINT NOT NULL REFERENCES place_categories(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_naver_category_mapping_naver_top ON naver_category_mapping(naver_top);

-- 초기 매핑 데이터: 네이버 최상단 키워드 → 앱 카테고리 이름 기반 JOIN.
INSERT INTO naver_category_mapping (naver_top, place_category_id)
SELECT x.naver_top, pc.id
FROM (VALUES
    -- 음식점
    ('한식', '음식점'),
    ('중식', '음식점'),
    ('양식', '음식점'),
    ('일식', '음식점'),
    ('분식', '음식점'),
    ('아시아음식', '음식점'),
    ('퓨전요리', '음식점'),
    ('뷔페', '음식점'),
    ('치킨', '음식점'),
    ('패스트푸드', '음식점'),
    ('술집', '음식점'),
    ('도시락', '음식점'),
    ('간식', '음식점'),
    -- 카페,디저트
    ('카페,디저트', '카페,디저트'),
    ('빵,케익,샌드위치', '카페,디저트'),
    -- 쇼핑,유통
    ('마트', '쇼핑,유통'),
    ('백화점', '쇼핑,유통'),
    ('의류', '쇼핑,유통'),
    ('신발', '쇼핑,유통'),
    -- 생활,편의
    ('편의점', '생활,편의'),
    ('세탁', '생활,편의'),
    ('미용실', '생활,편의'),
    ('부동산', '생활,편의'),
    -- 여행,숙박
    ('숙박', '여행,숙박'),
    ('펜션', '여행,숙박'),
    ('모텔', '여행,숙박'),
    ('호텔', '여행,숙박'),
    -- 문화,예술
    ('영화관', '문화,예술'),
    ('박물관', '문화,예술'),
    ('공연장', '문화,예술'),
    -- 교육,학문
    ('학원', '교육,학문'),
    ('학교', '교육,학문'),
    ('도서관', '교육,학문'),
    -- 의료,건강
    ('병원', '의료,건강'),
    ('의원', '의료,건강'),
    ('약국', '의료,건강')
) AS x(naver_top, app_name)
JOIN place_categories pc ON pc.name = x.app_name;

-- 기존 places에 대해 이 매핑 테이블로 추가 소급 업데이트 (V42에서 못 잡은 건들 보정)
UPDATE places p SET place_category_id = m.place_category_id
FROM naver_category_mapping m
WHERE p.place_category_id IS NULL
  AND p.category IS NOT NULL
  AND TRIM(SPLIT_PART(p.category, '>', 1)) = m.naver_top;
