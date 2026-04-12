-- 카페 카테고리에 트렌드 메뉴 태그 추가
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '메뉴', '두쫀쿠', 10 FROM place_categories WHERE name = '카페,디저트'
UNION ALL SELECT id, '메뉴', '버터떡', 11 FROM place_categories WHERE name = '카페,디저트';
