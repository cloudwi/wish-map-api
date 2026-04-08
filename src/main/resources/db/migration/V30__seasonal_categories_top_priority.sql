-- 붕어빵, 벚꽃 카테고리 최상단으로 이동
UPDATE place_categories SET priority = -2 WHERE name = '붕어빵';
UPDATE place_categories SET priority = -1 WHERE name = '벚꽃';
