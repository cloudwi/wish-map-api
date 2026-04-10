-- 붕어빵 카테고리 태그 추가
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '속재료', '치즈', 6 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '딸기', 7 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '누텔라', 8 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '속재료', '아이스크림', 9 FROM place_categories WHERE name = '붕어빵';

INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '쫀득한', 5 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '특징', '속 가득', 6 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '특징', '미니 사이즈', 7 FROM place_categories WHERE name = '붕어빵';

INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '한줄평', '겨울 필수', 3 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '한줄평', '간식으로 딱', 4 FROM place_categories WHERE name = '붕어빵'
UNION ALL SELECT id, '한줄평', '선물용', 5 FROM place_categories WHERE name = '붕어빵';
