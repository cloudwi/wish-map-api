-- 파티 중심 서비스로 전환하면서 기존 장소 기록/소셜 기능의 데이터를 제거한다.
-- 기존 사용자가 거의 없는 상태에서 데이터 삭제를 승인받았다.

DROP TABLE IF EXISTS lunch_vote_selections;
DROP TABLE IF EXISTS lunch_vote_candidates;
DROP TABLE IF EXISTS lunch_votes;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS friends;

DROP TABLE IF EXISTS comment_images;
DROP TABLE IF EXISTS comment_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS like_groups;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS categories;

ALTER TABLE places DROP COLUMN IF EXISTS description;
ALTER TABLE places DROP COLUMN IF EXISTS thumbnail_image;
ALTER TABLE places DROP COLUMN IF EXISTS price_range;
ALTER TABLE places DROP COLUMN IF EXISTS place_category_id;
ALTER TABLE places DROP COLUMN IF EXISTS suggested_by;

DROP TABLE IF EXISTS place_category_tags;
DROP TABLE IF EXISTS naver_category_mapping;
DROP TABLE IF EXISTS place_categories;
DROP TABLE IF EXISTS trend_tags;
DROP TABLE IF EXISTS _backup_places_v42;
DROP TABLE IF EXISTS restaurant_images;
DROP TABLE IF EXISTS places CASCADE;

DELETE FROM reports WHERE target_type IN ('COMMENT', 'RESTAURANT');
DELETE FROM notifications WHERE type IN (
    'GROUP_LOCATION_CHANGED', 'GROUP_INVITE', 'FRIEND_REQUEST',
    'LUNCH_VOTE_CREATED', 'LUNCH_VOTE_CLOSED'
);
