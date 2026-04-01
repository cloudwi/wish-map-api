-- 방문 기록에 가격대 정보 추가
ALTER TABLE visits ADD COLUMN price_range VARCHAR(20);

-- 맛집에 캐싱된 가격대 정보 추가 (최다 보고 가격대)
ALTER TABLE restaurants ADD COLUMN price_range VARCHAR(20);

-- 가격대 필터링용 인덱스
CREATE INDEX idx_restaurants_price_range ON restaurants(price_range);
