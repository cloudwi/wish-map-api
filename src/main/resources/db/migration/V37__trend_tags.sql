CREATE TABLE trend_tags (
    id         SERIAL PRIMARY KEY,
    label      VARCHAR(50) NOT NULL,
    category_name VARCHAR(50),
    tags       VARCHAR(500),
    price_range VARCHAR(20),
    priority   INT NOT NULL DEFAULT 0,
    active     BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO trend_tags (label, category_name, tags, price_range, priority) VALUES
('🌸 벚꽃', '벚꽃', NULL, NULL, 1),
('🐟 붕어빵', '붕어빵', NULL, NULL, 2),
('🍪 두쫀쿠', '카페,디저트', '두쫀쿠', NULL, 3),
('🧈 버터떡', '카페,디저트', '버터떡', NULL, 4),
('💰 거지맵', NULL, NULL, 'UNDER_10K', 5);
