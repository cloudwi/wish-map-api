-- 회원 탈퇴 시 places 의 FK 제약으로 user 삭제가 막히는 문제 해결.
-- places 는 동료들이 함께 보는 공유 데이터이므로 제보자가 탈퇴해도 장소는 유지하고
-- suggested_by 만 NULL 로 익명화한다 (개인 활동 기록 — 방문/리뷰/알림 — 은 서비스 레이어에서 명시 삭제).

ALTER TABLE places ALTER COLUMN suggested_by DROP NOT NULL;

-- 기존 FK 이름이 restaurants_suggested_by_fkey (V41 리네임 누락) 이므로 동적으로 찾아 제거.
DO $$
DECLARE
    fk_name TEXT;
BEGIN
    SELECT conname INTO fk_name
    FROM pg_constraint
    WHERE conrelid = 'places'::regclass
      AND contype = 'f'
      AND pg_get_constraintdef(oid) LIKE '%(suggested_by)%';
    IF fk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE places DROP CONSTRAINT %I', fk_name);
    END IF;
END $$;

ALTER TABLE places
    ADD CONSTRAINT places_suggested_by_fkey
    FOREIGN KEY (suggested_by) REFERENCES users(id) ON DELETE SET NULL;
