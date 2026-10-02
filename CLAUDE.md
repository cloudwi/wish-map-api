# Wish Map API

Spring Boot 4, Kotlin, PostgreSQL, Flyway 기반의 장소별 파티 모집 API.

## 서비스 규칙

- 비회원: 모집 중인 파티 목록·상세 조회, 장소 검색
- 로그인 회원: 파티 생성, 참가 신청, 신청 취소, 신고
- 주최자: 참가 승인·거절, 파티 취소
- 휴대폰 문자 인증으로 로그인한다. 이메일·소셜 로그인은 사용하지 않는다.
- 인증번호는 5분 유효하며 1분 재전송 대기, 전화번호별 시간당 5회 발송, 코드당 5회 입력 제한을 적용한다.
- 발송 총량은 하루 100건으로 제한한다. `SMS_MAX_DAILY_SENDS`로 변경할 수 있다.
- 정원에는 주최자를 포함하며 승인된 참가자만 계산. 승인 시 파티 행을 잠가 동시 승인 초과를 방지.

## 주요 경로

- `GET /api/v1/parties`, `GET /api/v1/parties/{id}`
- `POST /api/v1/auth/phone/request`, `POST /api/v1/auth/phone/verify`
- `POST /api/v1/parties`, `POST /api/v1/parties/{id}/join`
- `POST /api/v1/parties/{id}/members/{memberId}/approve|reject`
- `POST /api/v1/parties/{id}/withdraw|cancel`
- `GET /api/v1/search/places`: 네이버 지역 검색 프록시

## 데이터와 배포

- 파티는 장소 이름·주소·좌표를 스냅샷으로 보관한다. 기존 `places` 테이블은 사용하지 않는다.
- V47 마이그레이션은 이전 점심 투표·친구·그룹·방문 기록·장소 테이블과 데이터를 삭제한다.
- V48 마이그레이션은 `parties`, `party_members`를 생성한다.
- V49 마이그레이션은 기존 소셜 계정과 회원 데이터를 삭제하고 `phone_verifications`를 생성한다.
- Solapi 발송에는 `SOLAPI_API_KEY`, `SOLAPI_API_SECRET`, `SOLAPI_SENDER`가 필요하다.
- `./gradlew test`는 Testcontainers PostgreSQL에서 마이그레이션과 파티 흐름을 검증한다.
- 운영 API는 Render에서 배포한다. 배포 후 `/health`와 파티 공개 API를 확인한다.
