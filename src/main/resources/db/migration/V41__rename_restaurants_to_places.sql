-- restaurants → places 리네임 + 미사용 restaurant_images 테이블 제거

DROP TABLE IF EXISTS restaurant_images;

ALTER TABLE restaurants RENAME TO places;

ALTER TABLE bookmarks RENAME COLUMN restaurant_id TO place_id;
ALTER TABLE comments RENAME COLUMN restaurant_id TO place_id;
ALTER TABLE visits RENAME COLUMN restaurant_id TO place_id;
ALTER TABLE lunch_vote_candidates RENAME COLUMN restaurant_id TO place_id;

ALTER INDEX restaurants_pkey RENAME TO places_pkey;
ALTER INDEX restaurants_naver_place_id_key RENAME TO places_naver_place_id_key;
ALTER INDEX idx_restaurants_location RENAME TO idx_places_location;
ALTER INDEX idx_restaurants_place_category RENAME TO idx_places_place_category;
ALTER INDEX idx_restaurants_price_range RENAME TO idx_places_price_range;
ALTER INDEX idx_restaurants_suggested_by RENAME TO idx_places_suggested_by;

ALTER INDEX bookmarks_restaurant_id_user_id_key RENAME TO bookmarks_place_id_user_id_key;
ALTER INDEX idx_comments_restaurant RENAME TO idx_comments_place;
ALTER INDEX idx_visits_restaurant RENAME TO idx_visits_place;
ALTER INDEX idx_visits_restaurant_created RENAME TO idx_visits_place_created;
ALTER INDEX idx_visits_restaurant_user_date RENAME TO idx_visits_place_user_date;
ALTER INDEX lunch_vote_candidates_vote_id_restaurant_id_key RENAME TO lunch_vote_candidates_vote_id_place_id_key;

ALTER TABLE bookmarks RENAME CONSTRAINT bookmarks_restaurant_id_fkey TO bookmarks_place_id_fkey;
ALTER TABLE comments RENAME CONSTRAINT comments_restaurant_id_fkey TO comments_place_id_fkey;
ALTER TABLE visits RENAME CONSTRAINT visits_restaurant_id_fkey TO visits_place_id_fkey;
ALTER TABLE lunch_vote_candidates RENAME CONSTRAINT lunch_vote_candidates_restaurant_id_fkey TO lunch_vote_candidates_place_id_fkey;
