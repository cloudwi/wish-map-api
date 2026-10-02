-- 이전 소셜 계정은 새 전화번호 로그인으로 연결할 수 없으므로 기존 회원 데이터를 정리한다.
TRUNCATE TABLE users CASCADE;
DROP TABLE IF EXISTS social_accounts;
ALTER TABLE users ADD COLUMN phone VARCHAR(11) UNIQUE;

CREATE TABLE phone_verifications (
    phone VARCHAR(11) PRIMARY KEY,
    code_hash VARCHAR(64),
    expires_at TIMESTAMP,
    attempts INT NOT NULL DEFAULT 0,
    sent_count INT NOT NULL DEFAULT 0,
    window_started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sent_at TIMESTAMP
);

CREATE TABLE sms_daily_quota (
    day DATE PRIMARY KEY,
    sent_count INT NOT NULL
);
