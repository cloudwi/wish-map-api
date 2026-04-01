# Wish Map API

## 개요
회사 동료들과 함께 쓰는 맛집 공유 지도 서비스 백엔드. Spring Boot 4 + Kotlin + PostgreSQL.

## 기술 스택
- Java 21, Kotlin 2.2, Spring Boot 4.0.3
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA, Flyway
- DB: H2 (local), PostgreSQL (prod, Neon)
- 배포: Render (Docker)

## 프로젝트 구조
```
src/main/kotlin/com/mindbridge/wishmap/
├── controller/    # REST 컨트롤러
├── service/       # 비즈니스 로직
├── repository/    # JPA Repository
├── domain/
│   ├── common/    # BaseEntity, BaseTimeEntity
│   ├── user/      # User, SocialAccount, Friend
│   ├── restaurant/# Restaurant, Visit, Like, LikeGroup, RestaurantImage, PriceRange
│   ├── group/     # Group, GroupMember (그룹 시스템)
│   ├── comment/   # Comment, CommentImage
│   └── category/  # Category
├── dto/           # Request/Response DTO
├── security/      # JWT, UserPrincipal, Filter
├── config/        # SecurityConfig, WebClientConfig
└── exception/     # GlobalExceptionHandler, 커스텀 예외
```

## API 엔드포인트

### 인증
- `POST /api/v1/auth/{provider}` - 소셜 로그인 (KAKAO/GOOGLE/NAVER/APPLE)
- `POST /api/v1/auth/refresh` - 토큰 갱신

### 맛집
- `GET /api/v1/restaurants` - 맛집 목록 (지도 bounds 기반 + 가격대 필터)
- `GET /api/v1/restaurants/{id}` - 맛집 상세
- `POST /api/v1/restaurants` - 맛집 등록
- `POST /api/v1/restaurants/quick-visit` - 방문인증 (가격대 필수 + 리뷰/이미지 선택)
- `POST /api/v1/restaurants/{id}/visit` - 방문 인증 (기존)
- `GET /api/v1/restaurants/place-stats` - 장소 통계 (방문수, 가격대, 리뷰)

### 그룹
- `GET /api/v1/groups` - 내 그룹 목록
- `POST /api/v1/groups` - 그룹 생성
- `GET /api/v1/groups/{id}` - 그룹 상세 (멤버 목록)
- `POST /api/v1/groups/{id}/members` - 멤버 초대 (닉네임)
- `DELETE /api/v1/groups/{id}/members/{userId}` - 멤버 추방 (그룹장)
- `POST /api/v1/groups/{id}/transfer` - 그룹장 양도
- `DELETE /api/v1/groups/{id}/leave` - 그룹 탈퇴
- `GET /api/v1/groups/{id}/restaurants` - 그룹 맛집 (구성원 필터 + 가격대 필터)

### 컬렉션
- `GET /api/v1/restaurants/collections` - 내 컬렉션 목록
- `POST /api/v1/restaurants/collections` - 새 컬렉션 생성
- `PUT /api/v1/restaurants/{id}/collections` - 맛집 컬렉션 할당

### 검색
- `GET /api/v1/search/places` - 네이버 지역 검색 프록시

### 댓글
- `GET /api/v1/restaurants/{id}/comments` - 댓글 목록
- `POST /api/v1/restaurants/{id}/comments` - 댓글 작성 (이미지 첨부 가능)

## DB 스키마

### 주요 테이블
- `users` - 사용자 (nickname, role)
- `social_accounts` - 소셜 계정 (provider, providerId, email)
- `restaurants` - 맛집 (name, lat, lng, naverPlaceId, suggestedBy, price_range)
- `visits` - 방문 기록 (restaurant, user, rating, price_range)
- `comments` - 댓글 (태그는 #태그 형태로 content에 포함)
- `comment_images` - 댓글 이미지
- `groups` - 그룹 (name, leader)
- `group_members` - 그룹 멤버 (group, user, role: LEADER/MEMBER)
- `likes` - 좋아요 (likeGroup 기반)
- `like_groups` - 좋아요 그룹 (컬렉션)
- `friends` - 친구 관계

### 가격대 시스템
- Visit에 price_range 저장 (방문 시 필수 입력)
- Restaurant에 price_range 캐시 (최다 보고 가격대)
- PriceRange enum: UNDER_10K, RANGE_10K, RANGE_20K, RANGE_30K, OVER_30K

### Flyway 마이그레이션
- V1~V18: 초기 스키마 ~ 사용자 약관 컬럼
- V19: 가격대 컬럼 추가 (visits, restaurants)

## 엔티티 상속
- `BaseEntity`: id + createdAt + updatedAt → Restaurant, Comment, Group
- `BaseTimeEntity`: id + createdAt → User, SocialAccount, Visit, Like, GroupMember, Friend

## 인증 구조
- 클라이언트가 OAuth 토큰 전달 → 서버가 검증 → JWT 발급
- Access Token: 30분, Refresh Token: 7일
- 회원가입 시 랜덤 닉네임 자동 부여 ("배고픈판다123" 스타일)
- 한 사용자가 여러 소셜 계정 연동 가능

## 핵심 비즈니스 로직
### quickVisit (방문 인증 + 리뷰)
1. naverPlaceId로 기존 맛집 검색, 없으면 자동 등록
2. GPS 거리 체크 (100m 이내)
3. 당일 중복 방문 체크
4. Visit 저장 (priceRange 필수) + 선택적 Comment/이미지 저장
5. Restaurant.priceRange 캐시 업데이트

### 가격대 필터
- GET /restaurants, GET /groups/{id}/restaurants에 priceRange 쿼리 파라미터
- Restaurant.priceRange 기반 서버사이드 필터링

### 그룹 필터
- 그룹 구성원 ID 조회 → 해당 구성원이 suggestedBy이거나 visit한 맛집만 필터링

## 빌드 & 실행
```bash
# 로컬 실행 (H2) - 환경변수 필요
NAVER_SEARCH_CLIENT_ID=xxx NAVER_SEARCH_CLIENT_SECRET=xxx \
./gradlew bootRun --args='--spring.profiles.active=local'

# 빌드
./gradlew bootJar
```

## 환경변수
### 로컬
- NAVER_SEARCH_CLIENT_ID, NAVER_SEARCH_CLIENT_SECRET

### 프로덕션
- DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
- JWT_SECRET
- NAVER_SEARCH_CLIENT_ID, NAVER_SEARCH_CLIENT_SECRET
- SPRING_PROFILES_ACTIVE=prod

## 디자인 원칙
- **해외 서비스 스타일의 단순한 UX/UI를 추구** (미니멀, 깔끔, 직관적)
- API 응답은 필요한 데이터만 포함, 오버페칭 금지
- 에러 메시지는 사용자 친화적 한국어로

## 컨벤션
- 커밋: `feat:`, `fix:` 등 한국어 메시지
- Co-Authored-By 포함하지 않음
- 별점 시스템 사용하지 않음 (방문 카운트 기반)

## 알려진 이슈 / TODO
- **성능 병목 가능 지점**:
  - `batchWeeklyChampions()`: 주간 방문왕 계산이 매 요청마다 실행됨 → 캐싱(Redis) 또는 배치 처리 고려
  - `countLikesByRestaurants()`: 대량 데이터 시 N+1 문제 가능 → 인덱스 최적화 필요
  - 지도 bounds 기반 조회: 사용자 증가 시 공간 인덱스(PostGIS) 도입 고려
