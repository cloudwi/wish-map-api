-- 카테고리 테이블
CREATE TABLE categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    priority   INT         NOT NULL DEFAULT 0,
    active     BOOLEAN     NOT NULL DEFAULT true,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

-- 기본 카테고리 (priority 순서대로)
INSERT INTO categories (name, priority) VALUES
    ('한식', 1),
    ('중식', 2),
    ('일식', 3),
    ('양식', 4),
    ('카페', 5),
    ('술집', 6),
    ('분식', 7),
    ('패스트푸드', 8),
    ('디저트', 9),
    ('기타', 100);
