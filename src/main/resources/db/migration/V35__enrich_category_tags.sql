-- 기존 태그가 부족한 카테고리에 태그 그룹 추가

-- ===== 쇼핑,유통 =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '분위기', '구경하기 좋은', 10 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '분위기', '넓은', 11 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '분위기', '깔끔한', 12 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '분위기', '북적이는', 13 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '편의', '주차 편해', 10 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '편의', '교통 좋은', 11 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '편의', '시식 가능', 12 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '편의', '반품 쉬운', 13 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '한줄평', '또 갈 곳', 10 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '한줄평', '선물 사기 좋은', 11 FROM place_categories WHERE name = '쇼핑,유통'
UNION ALL SELECT id, '한줄평', '숨은 명소', 12 FROM place_categories WHERE name = '쇼핑,유통';

-- ===== 여행,숙박 =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '분위기', '힐링', 10 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '분위기', '로맨틱', 11 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '분위기', '가족 추천', 12 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '분위기', '감성적인', 13 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '편의', '주차 편해', 10 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '편의', '조식 맛있는', 11 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '편의', '체크인 빠른', 12 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '편의', '어메니티 좋은', 13 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '한줄평', '또 갈 곳', 10 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '한줄평', '인생 숙소', 11 FROM place_categories WHERE name = '여행,숙박'
UNION ALL SELECT id, '한줄평', '사진 맛집', 12 FROM place_categories WHERE name = '여행,숙박';

-- ===== 문화,예술 =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '분위기', '감성적인', 10 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '분위기', '신기한', 11 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '분위기', '웅장한', 12 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '편의', '주차 편해', 10 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '편의', '아이 동반 OK', 11 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '편의', '실내', 12 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '한줄평', '또 갈 곳', 10 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '한줄평', '사진 맛집', 11 FROM place_categories WHERE name = '문화,예술'
UNION ALL SELECT id, '한줄평', '숨은 명소', 12 FROM place_categories WHERE name = '문화,예술';

-- ===== 교육,학문 =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '분위기', '집중 잘 되는', 10 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '분위기', '체계적인', 11 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '분위기', '소규모', 12 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '편의', '주차 편해', 10 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '편의', '교통 좋은', 11 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '한줄평', '배움이 많은', 10 FROM place_categories WHERE name = '교육,학문'
UNION ALL SELECT id, '한줄평', '추천해요', 11 FROM place_categories WHERE name = '교육,학문';

-- ===== 의료,건강 =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '분위기', '쾌적한', 10 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '분위기', '조용한', 11 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '편의', '예약 쉬운', 10 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '편의', '주차 편해', 11 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '편의', '대기 짧은', 12 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '한줄평', '믿고 가는 곳', 10 FROM place_categories WHERE name = '의료,건강'
UNION ALL SELECT id, '한줄평', '추천해요', 11 FROM place_categories WHERE name = '의료,건강';

-- ===== 벚꽃 (custom) =====
INSERT INTO place_category_tags (category_id, tag_group, tag, priority)
SELECT id, '특징', '만개', 1 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '특징', '산책로 좋은', 2 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '특징', '야경 예쁜', 3 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '특징', '한적한', 4 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '특징', '포토스팟', 5 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '한줄평', '또 갈 곳', 1 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '한줄평', '인생 뷰', 2 FROM place_categories WHERE name = '벚꽃'
UNION ALL SELECT id, '한줄평', '숨은 명소', 3 FROM place_categories WHERE name = '벚꽃';
