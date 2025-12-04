# SALM v3.0

살림 정보 공유 + 상품 연결 플랫폼

## 🚀 주요 특징

- **웹 + 앱 동시 지원**: Session(웹) + JWT(앱) 듀얼 인증
- **OAuth 스탠바이**: Google/Kakao/Naver 설정만 하면 바로 활성화
- **확장성 고려**: 모듈 구조로 코디 추천 등 기능 추가 용이
- **보안 강화**: XSS 방지, CSRF, BCrypt(12), 파일 검증

## 📁 프로젝트 구조

```
salm/
├── src/main/java/kr/salm/
│   ├── config/           # 보안, MVC 설정
│   ├── core/             # 공통 (Entity, DTO, Exception, Util)
│   ├── auth/             # 인증/회원
│   ├── community/        # 게시글, 댓글, 좋아요, 북마크
│   ├── product/          # 상품 연동 (향후 확장)
│   └── file/             # 파일 업로드
├── src/main/resources/
│   ├── templates/        # Thymeleaf 템플릿
│   ├── static/           # CSS, JS
│   └── application.yml   # 설정
├── build.gradle.kts
└── .env.example          # 환경변수 템플릿
```

## ⚙️ 설치 및 실행

### 1. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 실제 값 입력
```

### 2. Gradle Wrapper 다운로드

```bash
# gradle-wrapper.jar 다운로드 필요 (바이너리라 포함 안됨)
gradle wrapper
# 또는 직접 다운로드: 
# https://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### 3. 데이터베이스 생성

```sql
CREATE DATABASE salm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'salm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON salm.* TO 'salm_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. 빌드 및 실행

```bash
# 환경변수 로드 후 실행
export $(cat .env | xargs) && ./gradlew bootRun

# 또는 JAR 빌드
./gradlew build -x test
java -jar build/libs/salm.jar
```

### 5. 접속

- 웹: http://localhost:8080
- API: http://localhost:8080/api/...

## 🔐 보안 체크리스트

| 항목 | 상태 |
|------|------|
| 환경변수 분리 | ✅ |
| CSRF 토큰 | ✅ |
| XSS 방지 (OWASP Encoder) | ✅ |
| 파일 업로드 검증 | ✅ |
| BCrypt (strength 12) | ✅ |
| 세션 관리 | ✅ |
| 보안 헤더 (CSP, XSS) | ✅ |

## 📡 API 엔드포인트

### 인증
```
POST /api/auth/signup      # 회원가입
POST /api/auth/login       # 로그인 (JWT 발급)
POST /api/auth/refresh     # 토큰 갱신
POST /api/auth/logout      # 로그아웃
GET  /api/auth/me          # 내 정보
GET  /api/auth/check/*     # 중복 확인
```

### 게시글
```
GET  /api/posts            # 목록 (페이징)
GET  /api/posts/latest     # 최신
GET  /api/posts/popular    # 인기
GET  /api/posts/{id}       # 상세
GET  /api/posts/search     # 검색
POST /api/posts            # 작성 (인증)
PUT  /api/posts/{id}       # 수정 (인증)
DELETE /api/posts/{id}     # 삭제 (인증)
```

### 댓글/좋아요/북마크
```
GET  /api/posts/{id}/comments      # 댓글 목록
POST /api/posts/{id}/comments      # 댓글 작성
DELETE /api/posts/{postId}/comments/{commentId}  # 댓글 삭제
POST /api/posts/{id}/like          # 좋아요 토글
POST /api/posts/{id}/bookmark      # 북마크 토글
```

## 🔧 OAuth 활성화 방법

`.env`에 키 입력하면 자동 활성화:

```bash
# Google
OAUTH_GOOGLE_CLIENT_ID=your-client-id
OAUTH_GOOGLE_CLIENT_SECRET=your-secret

# Kakao
OAUTH_KAKAO_CLIENT_ID=your-rest-api-key
OAUTH_KAKAO_CLIENT_SECRET=your-secret

# Naver
OAUTH_NAVER_CLIENT_ID=your-client-id
OAUTH_NAVER_CLIENT_SECRET=your-secret
```

Redirect URI 설정: `https://salm.kr/oauth2/callback/{provider}`

## 📱 앱 연동

모든 `/api/**` 엔드포인트는 JWT 인증 지원:

```
Authorization: Bearer {access_token}
```

## 🏗️ 향후 확장 계획

- [ ] 상품 연동 (쿠팡 파트너스 API)
- [ ] 코디 추천 모듈
- [ ] 알림 기능
- [ ] 관리자 페이지

---

© 2025 SALM
