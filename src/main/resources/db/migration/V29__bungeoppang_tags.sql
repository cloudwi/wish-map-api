-- 붕어빵 카테고리 태그 추가
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '속재료', '팥', 1 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '슈크림', 2 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '고구마', 3 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '피자', 4 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '초코', 5 FROM place_categories WHERE name = '붕어빵';

INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '갓구운', 1 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '특징', '바삭한', 2 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '특징', '줄 서는 곳', 3 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '특징', '가성비', 4 FROM place_categories WHERE name = '붕어빵';

INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '한줄평', '또 갈 곳', 1 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '한줄평', '숨은 맛집', 2 FROM place_categories WHERE name = '붕어빵';

-- 가격대 태그 추가 (has_price_range=TRUE)
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '가격대', '1만원 이하', 1 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '가격대', '1만원대', 2 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '가격대', '2만원대', 3 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '가격대', '3만원대', 4 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '가격대', '3만원 이상', 5 FROM place_categories WHERE name = '붕어빵';
