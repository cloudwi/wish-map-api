-- 기존 NULL 데이터를 기본값(1만원대)으로 채우기
UPDATE visits SET price_range = 'RANGE_10K' WHERE price_range IS NULL;
UPDATE restaurants SET price_range = 'RANGE_10K' WHERE price_range IS NULL;

-- NOT NULL 제약 추가
ALTER TABLE visits ALTER COLUMN price_range SET NOT NULL;
ALTER TABLE restaurants ALTER COLUMN price_range SET NOT NULL;
