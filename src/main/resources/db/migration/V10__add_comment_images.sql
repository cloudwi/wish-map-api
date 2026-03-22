-- 리뷰 이미지 테이블
CREATE TABLE comment_images (
    id            BIGSERIAL PRIMARY KEY,
    comment_id    BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    image_url     VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_comment_images_comment_id ON comment_images(comment_id);
