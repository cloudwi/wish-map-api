-- 1.0.4 강제 업데이트
UPDATE app_version_control
SET min_version = '1.0.4',
    latest_version = '1.0.4',
    force_update = TRUE,
    updated_at = NOW();
