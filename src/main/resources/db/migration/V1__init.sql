-- 사용자
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    nickname    VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (email, provider)
);

-- 맛집
CREATE TABLE restaurants (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(500) NOT NULL,
    lat             DOUBLE PRECISION NOT NULL,
    lng             DOUBLE PRECISION NOT NULL,
    naver_place_id  VARCHAR(255) UNIQUE,
    category        VARCHAR(100),
    description     TEXT,
    thumbnail_image VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    suggested_by    BIGINT NOT NULL REFERENCES users(id),
    approved_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_restaurants_suggested_by ON restaurants(suggested_by);

-- 맛집 이미지
CREATE TABLE restaurant_images (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    image_url     VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_restaurant_images_restaurant ON restaurant_images(restaurant_id);

-- 좋아요
CREATE TABLE likes (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, user_id)
);

CREATE INDEX idx_likes_user ON likes(user_id);

-- 북마크
CREATE TABLE bookmarks (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, user_id)
);

CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);

-- 댓글
CREATE TABLE comments (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content       TEXT NOT NULL,
    is_deleted    BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_restaurant ON comments(restaurant_id);
CREATE INDEX idx_comments_user ON comments(user_id);
