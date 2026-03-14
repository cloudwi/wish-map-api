# Wish Map API

## 개요
맛집 공유 지도 서비스 백엔드. Spring Boot 4 + Kotlin + PostgreSQL.

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
├── domain/        # 엔티티
│   ├── common/    # BaseEntity, BaseTimeEntity
│   ├── user/      # User, SocialAccount, AuthProvider, NicknameGenerator
│   ├── restaurant/# Restaurant, Like, Bookmark, RestaurantImage
│   └── comment/   # Comment
├── dto/           # Request/Response DTO
├── security/      # JWT, UserPrincipal, Filter
├── config/        # SecurityConfig, WebClientConfig
└── exception/     # GlobalExceptionHandler, 커스텀 예외
```

## API 엔드포인트
- `POST /api/v1/auth/{provider}` - 소셜 로그인 (KAKAO/GOOGLE/NAVER/APPLE)
- `POST /api/v1/auth/refresh` - 토큰 갱신
- `GET /api/v1/restaurants` - 맛집 목록 (지도 bounds 기반)
- `GET /api/v1/restaurants/{id}` - 맛집 상세
- `POST /api/v1/restaurants` - 맛집 등록
- `POST /api/v1/restaurants/{id}/like` - 좋아요 토글
- `POST /api/v1/restaurants/{id}/bookmark` - 북마크 토글
- `GET /api/v1/restaurants/bookmarks` - 북마크 목록
- `GET/POST /api/v1/restaurants/{id}/comments` - 댓글
- `GET /api/v1/admin/restaurants/pending` - 관리자: 승인 대기

## DB 스키마 (Flyway)
- V1: 초기 스키마 (users, restaurants, restaurant_images, likes, bookmarks, comments)
- V2: social_accounts 테이블 분리 (users에서 provider/email 분리)

## 엔티티 상속
- `BaseEntity`: id + createdAt + updatedAt → Restaurant, Comment
- `BaseTimeEntity`: id + createdAt → User, SocialAccount, Bookmark, Like
- `RestaurantImage`: 타임스탬프 없음 (상속 없음)

## 인증 구조
- 클라이언트가 OAuth 토큰 전달 → 서버가 검증 → JWT 발급
- Access Token: 30분, Refresh Token: 7일
- 회원가입 시 랜덤 닉네임 자동 부여 ("배고픈판다123" 스타일)
- 한 사용자가 여러 소셜 계정 연동 가능

## 빌드 & 실행
```bash
# 로컬 실행 (H2)
./gradlew bootRun --args='--spring.profiles.active=local'

# 빌드
./gradlew bootJar
```

## 환경변수 (prod)
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET, SPRING_PROFILES_ACTIVE=prod

## 컨벤션
- 커밋: `feat:`, `fix:` 등 한국어 메시지
- Co-Authored-By 포함하지 않음
