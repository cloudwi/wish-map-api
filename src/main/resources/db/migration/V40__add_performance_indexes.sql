-- 장소 카테고리 필터 성능 개선
CREATE INDEX IF NOT EXISTS idx_restaurants_place_category ON restaurants(place_category_id);

-- 주간 챔피언 및 인기순 정렬 성능 개선
CREATE INDEX IF NOT EXISTS idx_visits_restaurant_created ON visits(restaurant_id, created_at DESC);
