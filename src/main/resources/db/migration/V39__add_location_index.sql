-- 지도 bounds 쿼리 최적화: lat + lng 복합 인덱스
CREATE INDEX idx_restaurants_location ON restaurants (lat, lng);

-- 태그 기반 필터 최적화: tag + comment_id 복합 인덱스
CREATE INDEX idx_comment_tags_tag_comment ON comment_tags (tag, comment_id);
