-- 중복 place_category_tags 정리 (같은 category_id + tag_group + tag)
DELETE FROM place_category_tags
WHERE id NOT IN (
    SELECT MIN(id) FROM place_category_tags
    GROUP BY category_id, tag_group, tag
);

-- 유니크 제약 추가 (같은 카테고리에 동일 태그 중복 방지)
CREATE UNIQUE INDEX idx_place_category_tags_unique
ON place_category_tags (category_id, tag_group, tag);

-- 중복 trend_tags 정리 (같은 label)
DELETE FROM trend_tags
WHERE id NOT IN (
    SELECT MIN(id) FROM trend_tags
    GROUP BY label
);

-- 유니크 제약 추가
CREATE UNIQUE INDEX idx_trend_tags_label_unique ON trend_tags (label);
