-- 앱 버전 관리 테이블
CREATE TABLE app_version_control (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(10) NOT NULL UNIQUE,
    min_version VARCHAR(20) NOT NULL,
    latest_version VARCHAR(20) NOT NULL,
    force_update BOOLEAN NOT NULL DEFAULT FALSE,
    store_url TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO app_version_control (platform, min_version, latest_version, force_update, store_url) VALUES
('ios', '1.0.0', '1.0.1', FALSE, 'https://apps.apple.com/app/id6760577746'),
('android', '1.0.0', '1.0.1', FALSE, 'https://play.google.com/store/apps/details?id=com.wishmap.app');
