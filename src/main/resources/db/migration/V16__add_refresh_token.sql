-- Apple Sign-In 토큰 해지를 위해 refresh_token 저장
ALTER TABLE social_accounts ADD COLUMN refresh_token VARCHAR(1000);
