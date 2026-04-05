-- 음식점 카테고리에 가격대를 태그 그룹으로 추가
INSERT INTO place_category_tags (category_id, tag_group, tag, priority) VALUES
(1, '가격대', '1만원 이하', 1),
(1, '가격대', '1만원대', 2),
(1, '가격대', '2만원대', 3),
(1, '가격대', '3만원대', 4),
(1, '가격대', '3만원 이상', 5);

-- has_price_range 더 이상 불필요하지만 기존 컬럼은 유지 (하위호환)
