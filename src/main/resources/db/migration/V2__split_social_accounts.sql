-- 소셜 계정 테이블 생성
CREATE TABLE social_accounts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (provider, provider_id)
);

CREATE INDEX idx_social_accounts_user ON social_accounts(user_id);

-- 기존 users 데이터를 social_accounts로 이관
INSERT INTO social_accounts (user_id, provider, provider_id, email, created_at)
SELECT id, provider, provider_id, email, created_at
FROM users
WHERE provider IS NOT NULL;

-- users 테이블에서 소셜 관련 컬럼 제거
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_provider_key;
ALTER TABLE users DROP COLUMN IF EXISTS email;
ALTER TABLE users DROP COLUMN IF EXISTS provider;
ALTER TABLE users DROP COLUMN IF EXISTS provider_id;

-- nickname에 unique 제약조건 추가
ALTER TABLE users ADD CONSTRAINT users_nickname_key UNIQUE (nickname);
