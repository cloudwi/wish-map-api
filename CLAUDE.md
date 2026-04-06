# Wish Map API

## 개요
회사 동료들과 함께 쓰는 맛집 공유 지도 서비스 백엔드. Spring Boot 4 + Kotlin + PostgreSQL.

## 기술 스택
- Java 21, Kotlin 2.2, Spring Boot 4.0.3
- Spring Security + JWT (jjwt 0.12.6, nimbus-jose-jwt 10.3)
- Spring Data JPA, Flyway
- DB: PostgreSQL (local + prod), H2 (테스트용)
- 이미지 저장: Supabase Storage
- 배포: Render (Docker, multi-stage build)

## 프로젝트 구조
```
src/main/kotlin/com/mindbridge/wishmap/
├── controller/        # REST 컨트롤러
├── service/           # 비즈니스 로직
├── repository/        # JPA Repository
├── domain/
│   ├── common/        # BaseEntity, BaseTimeEntity
│   ├── user/          # User, SocialAccount, Friend, NicknameGenerator
│   ├── restaurant/    # Restaurant, Visit, Like, LikeGroup, RestaurantImage, PriceRange
│   ├── group/         # Group, GroupMember
│   ├── comment/       # Comment, CommentImage, CommentTag
│   ├── category/      # Category (기본 카테고리)
│   ├── placecategory/ # PlaceCategory, PlaceCategoryTag (태그 그룹 기반)
│   ├── moderation/    # Report, BlockedUser, UserAgreement
│   ├── notification/  # Notification, NotificationType
│   └── appversion/    # AppVersionControl (강제 업데이트)
├── dto/               # Request/Response DTO
├── security/          # JWT, UserPrincipal, Filter
├── config/            # SecurityConfig, WebClientConfig, AppVersionFilter, RequestLoggingFilter
└── exception/         # GlobalExceptionHandler, 커스텀 예외
```

## API 엔드포인트

### 인증 (`/api/v1/auth`)
- `POST /{provider}` - 소셜 로그인 (KAKAO/GOOGLE/NAVER/APPLE)
- `POST /refresh` - 토큰 갱신
- `PATCH /me/nickname` - 닉네임 변경
- `PATCH /me/push-token` - 푸시 토큰 업데이트
- `DELETE /me` - 회원 탈퇴

### 맛집 (`/api/v1/restaurants`)
- `GET /` - 맛집 목록 (bounds, category, placeCategoryId, tag, priceRange, search, sortBy)
- `GET /{id}` - 맛집 상세
- `POST /` - 맛집 등록
- `GET /my` - 내 맛집 (페이지네이션)
- `POST /{id}/visit` - 방문 인증 (GPS)
- `POST /quick-visit` - 방문인증 + 리뷰 (가격대 필수)
- `GET /place-stats` - 장소 통계 (naverPlaceId 기반)
- `GET /collections` - 컬렉션 목록
- `POST /collections` - 컬렉션 생성
- `GET /collections/{groupId}/restaurants` - 컬렉션 맛집
- `PUT /{id}/collections` - 맛집 컬렉션 할당

### 댓글 (`/api/v1`)
- `GET /restaurants/{id}/comments` - 댓글 목록
- `POST /restaurants/{id}/comments` - 댓글 작성 (태그 + 이미지)
- `PATCH /comments/{id}` - 댓글 수정
- `DELETE /comments/{id}` - 댓글 삭제

### 그룹 (`/api/v1/groups`)
- `GET /` - 내 그룹 목록
- `POST /` - 그룹 생성
- `GET /{id}` - 그룹 상세
- `POST /{id}/members` - 멤버 초대
- `DELETE /{id}/members/{userId}` - 멤버 추방
- `POST /{id}/transfer` - 그룹장 양도
- `DELETE /{id}/leave` - 그룹 탈퇴
- `GET /invites` - 받은 초대 목록
- `PATCH /{id}/invites/accept` - 초대 수락
- `PATCH /{id}/invites/reject` - 초대 거절
- `PATCH /{id}/location` - 그룹 위치 설정
- `GET /{id}/restaurants` - 그룹 맛집 (구성원 필터 + 태그 필터)

### 친구 (`/api/v1/friends`)
- `GET /search` - 사용자 검색
- `GET /` - 친구 목록
- `GET /requests` - 받은 요청 목록
- `POST /request/{userId}` - 친구 요청
- `PATCH /request/{friendId}/accept` - 수락
- `PATCH /request/{friendId}/reject` - 거절
- `DELETE /{friendId}` - 친구 삭제

### 알림 (`/api/v1/notifications`)
- `GET /` - 알림 목록
- `GET /unread-count` - 읽지 않은 수
- `PATCH /{id}/read` - 읽음 처리
- `PATCH /read-all` - 전체 읽음

### 장소 카테고리 (`/api/v1/place-categories`)
- `GET /` - 카테고리 + 태그 그룹 목록

### 신고/차단 (`/api/v1`)
- `POST /reports` - 신고
- `POST /users/{userId}/block` - 차단
- `DELETE /users/{userId}/block` - 차단 해제
- `GET /blocked-users` - 차단 목록

### 검색
- `GET /api/v1/search/places` - 네이버 지역 검색 프록시

### 기타
- `GET /health` - 헬스체크

## DB 스키마

### 주요 테이블
- `users` - 사용자 (nickname, profileImage, pushToken, role)
- `social_accounts` - 소셜 계정 (provider, providerId, email)
- `restaurants` - 맛집 (name, lat, lng, naverPlaceId, placeCategoryId, priceRange, suggestedBy)
- `visits` - 방문 기록 (restaurant, user, rating, priceRange)
- `comments` - 댓글 (restaurant, user, content, isDeleted)
- `comment_tags` - 댓글 태그 (tag, category) — content에서 분리된 구조화된 태그
- `comment_images` - 댓글 이미지
- `place_categories` - 장소 카테고리 (name, icon, hasPriceRange, active)
- `place_category_tags` - 태그 그룹별 태그 (tagGroup, tag, priority)
- `groups` - 그룹 (name, leader, baseLat, baseLng, baseAddress, baseRadius)
- `group_members` - 그룹 멤버 (role, status: INVITED/ACCEPTED/REJECTED)
- `likes` - 좋아요 (likeGroup 기반)
- `like_groups` - 컬렉션
- `friends` - 친구 관계 (status: PENDING/ACCEPTED/REJECTED)
- `notifications` - 알림 (type, title, message, isRead, referenceId)
- `reports` - 신고 (targetType, targetId, reason, status)
- `blocked_users` - 차단
- `user_agreements` - 약관 동의
- `app_version_control` - 앱 버전 관리 (platform, minVersion, latestVersion, forceUpdate)

### 태그 시스템
댓글 태그와 장소 카테고리 태그를 별도 테이블로 관리하는 구조:
- `comment_tags`: 댓글에 달린 태그 (기존 #태그 형식에서 별도 테이블로 마이그레이션)
- `place_category_tags`: 카테고리별 태그 그룹 (분위기, 맛 특징, 편의, 한줄평, 가격대)
- 맛집 필터: `?tag=<태그명>` → comment_tags LEFT JOIN 기반 필터링

### 장소 카테고리 (place_categories)
| 카테고리 | 아이콘 | 가격대 | 태그 그룹 |
|---------|-------|--------|----------|
| 음식 | 🍽 | O | 분위기, 맛 특징, 편의, 한줄평, 가격대 |
| 카페 | ☕ | X | 분위기, 메뉴, 한줄평 |
| 디저트/간식 | 🍞 | X | |
| 자연/풍경 | 🌸 | X | |
| 생활편의 | 🔧 | X | |

### 가격대 시스템
- Visit에 priceRange 저장 (방문 시 입력)
- Restaurant에 priceRange 캐시 (최다 보고 가격대)
- PriceRange enum: UNDER_10K, RANGE_10K, RANGE_20K, RANGE_30K, OVER_30K
- 음식 카테고리에서는 "가격대" 태그 그룹으로도 제공 (1만원 이하 ~ 3만원 이상)

### Flyway 마이그레이션
- V1~V18: 초기 스키마 ~ 사용자 약관
- V19~V20: 가격대 컬럼 추가 + NOT NULL
- V21: comment_tags 테이블 (content에서 태그 추출/분리)
- V22: place_categories + place_category_tags (계층형 카테고리-태그 시스템)
- V23: 네이버 카테고리 데이터
- V24: app_version_control (강제 업데이트)
- V25: 카페 카테고리 가격대 비활성화
- V26: 가격대를 태그 그룹으로 추가

## 엔티티 상속
- `BaseEntity`: id + createdAt + updatedAt → Restaurant, Comment, Group, GroupMember
- `BaseTimeEntity`: id + createdAt → User, SocialAccount, Visit, Like, Friend

## 인증 구조
- 클라이언트가 OAuth 토큰 전달 → 서버가 검증 → JWT 발급
- Access Token: 30분, Refresh Token: 7일
- 회원가입 시 랜덤 닉네임 자동 부여 ("배고픈판다123" 스타일)
- 한 사용자가 여러 소셜 계정 연동 가능
- Apple Sign-In: nimbus-jose-jwt로 ID Token 검증

## 핵심 비즈니스 로직

### quickVisit (방문 인증 + 리뷰)
1. naverPlaceId로 기존 맛집 검색, 없으면 자동 등록
2. GPS 거리 체크 (100m 이내)
3. 당일 중복 방문 체크
4. Visit 저장 (priceRange 필수) + 선택적 Comment/이미지/태그 저장
5. Restaurant.priceRange 캐시 업데이트

### 태그 필터링
- `GET /restaurants?tag=<태그명>` → comment_tags JOIN으로 해당 태그가 달린 맛집 필터
- placeCategoryId + tag 조합으로 카테고리별 태그 필터 가능

### 그룹 필터
- 그룹 구성원 ID 조회 → 해당 구성원이 suggestedBy이거나 visit한 맛집만 필터링

### 주간 방문왕 (weeklyChampion)
- 이번 주(월~일, 서울 시간대) 가장 많이 방문한 사용자 계산
- RestaurantListResponse에 weeklyChampion 필드로 포함

## 빌드 & 실행
```bash
# 로컬 실행 (PostgreSQL) - 환경변수 필요
NAVER_SEARCH_CLIENT_ID=xxx NAVER_SEARCH_CLIENT_SECRET=xxx \
./gradlew bootRun --args='--spring.profiles.active=local'

# 빌드
./gradlew bootJar
```

## 환경변수
### 로컬
- NAVER_SEARCH_CLIENT_ID, NAVER_SEARCH_CLIENT_SECRET
- 로컬 PostgreSQL: localhost:5432/wishmap (user: wishmap, pw: wishmap)

### 프로덕션
- DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
- JWT_SECRET
- NAVER_SEARCH_CLIENT_ID, NAVER_SEARCH_CLIENT_SECRET
- SUPABASE_KEY (이미지 저장)
- KAKAO_CLIENT_ID, GOOGLE_CLIENT_ID
- NAVER_CLIENT_ID, NAVER_CLIENT_SECRET (OAuth용)
- APPLE_CLIENT_ID, APPLE_KEY_ID, APPLE_TEAM_ID
- SPRING_PROFILES_ACTIVE=prod

## 디자인 원칙
- **해외 서비스 스타일의 단순한 UX/UI를 추구** (미니멀, 깔끔, 직관적)
- API 응답은 필요한 데이터만 포함, 오버페칭 금지
- 에러 메시지는 사용자 친화적 한국어로
- 별점 시스템 사용하지 않음 (방문 카운트 기반)

## API 버전 관리
- 현재 모든 API는 `/api/v1/` 하위
- Breaking change 발생 시 `/api/v2/` 신설, v1은 유지
- **Controller, DTO만 버전 분리** — Service/Repository/Entity는 공유
- 변경이 필요한 엔드포인트만 v2로 생성 (전체 복사 X)
- Breaking change 배포 순서: 백엔드 배포 (v1+v2 공존) → 앱 배포 → `app_version_control.min_version` 업데이트 → 충분한 시간 후 v1 제거

### 버전 공존 시 패키지 구조
```
controller/
├── v1/                # 기존 컨트롤러
│   └── RestaurantController.kt
├── v2/                # breaking change 엔드포인트만
│   └── RestaurantController.kt
dto/
├── v1/
│   └── RestaurantDto.kt
├── v2/
│   └── RestaurantDto.kt
service/               # 버전 무관, 공유
repository/            # 버전 무관, 공유
```

### 하위 호환성 규칙
- 응답 필드 삭제/이름 변경 금지 (새 필드 추가는 OK)
- 필수 요청 파라미터 추가 금지 (새 파라미터는 optional + 기본값)
- 기존 엔드포인트 경로/메서드 변경 금지

## 컨벤션
- 커밋: `feat:`, `fix:` 등 한국어 메시지
- Co-Authored-By 포함하지 않음

## 알려진 이슈 / TODO
- 테스트 코드 부족 (contextLoads만 존재)
- `batchWeeklyChampions()`: 주간 방문왕 계산이 매 요청마다 실행됨 → 캐싱 또는 배치 처리 고려
- `countLikesByRestaurants()`: 대량 데이터 시 N+1 가능 → 인덱스 최적화 필요
- 지도 bounds 기반 조회: 사용자 증가 시 공간 인덱스(PostGIS) 도입 고려
- Admin 엔드포인트 미구현 (컨트롤러만 존재)
