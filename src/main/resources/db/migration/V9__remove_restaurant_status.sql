-- Restaurant에서 status, approved_by 컬럼 제거
-- 모든 맛집은 등록 즉시 표시됨

ALTER TABLE restaurants DROP COLUMN IF EXISTS status;
ALTER TABLE restaurants DROP COLUMN IF EXISTS approved_by;
