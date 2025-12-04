# SALM v3.0

살림 정보 공유 플랫폼 - 일상 속 살림 노하우를 공유하고 상품을 연결하는 커뮤니티

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.2, Java 17 |
| Database | MariaDB 10.x (utf8mb4) |
| Auth | Session(웹) + JWT(앱) |
| Frontend | Thymeleaf + Tailwind CSS |
| Security | OWASP Encoder, BCrypt, CSRF |

## 디렉토리 구조
```
/app/salm/
├── salm/                    # 소스코드 (Git repo)
│   └── src/main/java/kr/salm/
│       ├── auth/            # 인증/회원
│       ├── community/       # 게시글/댓글/좋아요
│       ├── product/         # 상품 연동 (예정)
│       ├── file/            # 파일 업로드
│       ├── core/            # 공통 (Entity, DTO, Exception)
│       └── config/          # 설정
├── shared/                  # 공유 리소스
│   ├── .env                 # 환경변수
│   └── uploads/             # 업로드 파일
├── backups/                 # DB/파일 백업
├── scripts/                 # 운영 스크립트
└── logs/                    # 로그
```

## 설치 & 실행

### 1. 환경변수 설정
```bash
vi /app/salm/shared/.env
```
```env
SPRING_PROFILES_ACTIVE=local
DB_HOST=localhost
DB_PORT=3306
DB_NAME=salm
DB_USERNAME=salm_user
DB_PASSWORD=your_password
JPA_DDL_AUTO=update
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
FILE_UPLOAD_DIR=/app/salm/shared/uploads
SERVER_PORT=8080
```

### 2. DB 생성
```sql
CREATE DATABASE salm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'salm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON salm.* TO 'salm_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 실행
```bash
# systemd 서비스로 실행
systemctl start salm

# 또는 직접 실행
cd /app/salm/salm
export $(cat /app/salm/shared/.env | xargs) && ./gradlew bootRun
```

## 서비스 관리
```bash
systemctl start salm      # 시작
systemctl stop salm       # 중지
systemctl restart salm    # 재시작
systemctl status salm     # 상태 확인

# 로그 확인
tail -f /app/salm/logs/app.log
journalctl -u salm -f
```

## 운영 스크립트
```bash
# 백업 (DB + 업로드 파일)
/app/salm/scripts/backup.sh

# 배포 (git pull + 빌드 + 재시작)
/app/salm/scripts/deploy.sh
```

## API 엔드포인트

### 인증 (앱용 - JWT)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (토큰 발급) |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/auth/me` | 내 정보 |
| GET | `/api/auth/check/username` | 아이디 중복확인 |
| GET | `/api/auth/check/email` | 이메일 중복확인 |
| GET | `/api/auth/check/nickname` | 닉네임 중복확인 |

### 게시글

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/posts` | 목록 (페이징) |
| GET | `/api/posts/latest` | 최신 게시글 |
| GET | `/api/posts/popular` | 인기 게시글 |
| GET | `/api/posts/{id}` | 상세 조회 |
| GET | `/api/posts/search?keyword=` | 검색 |
| POST | `/api/posts` | 작성 🔒 |
| PUT | `/api/posts/{id}` | 수정 🔒 |
| DELETE | `/api/posts/{id}` | 삭제 🔒 |

### 댓글/좋아요/북마크

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/posts/{id}/comments` | 댓글 목록 |
| POST | `/api/posts/{id}/comments` | 댓글 작성 🔒 |
| DELETE | `/api/posts/{id}/comments/{commentId}` | 댓글 삭제 🔒 |
| POST | `/api/posts/{id}/like` | 좋아요 토글 🔒 |
| POST | `/api/posts/{id}/bookmark` | 북마크 토글 🔒 |

> 🔒 = 인증 필요

## 보안 체크리스트

- [x] 환경변수로 민감정보 분리
- [x] XSS 방지 (OWASP Encoder)
- [x] CSRF 토큰 (웹)
- [x] BCrypt 해싱 (strength 12)
- [x] 파일 업로드 검증 (확장자/MIME/크기)
- [x] 보안 헤더 (CSP, X-Frame-Options)
- [x] 세션 보안 (HttpOnly, 동시접속 제한)
- [x] SQL Injection 방지 (JPA Parameterized Query)

## OAuth 설정 (선택)

`.env`에 추가:
```env
OAUTH_GOOGLE_CLIENT_ID=your_client_id
OAUTH_GOOGLE_CLIENT_SECRET=your_client_secret
OAUTH_KAKAO_CLIENT_ID=your_client_id
OAUTH_KAKAO_CLIENT_SECRET=your_client_secret
```

Redirect URI 설정:
- Google: `https://salm.kr/login/oauth2/code/google`
- Kakao: `https://salm.kr/login/oauth2/code/kakao`

## 카테고리

| slug | 이름 | 아이콘 |
|------|------|--------|
| daily | 일상 | home |
| kitchen | 주방 | kitchen |
| bathroom | 욕실 | bathroom |
| cleaning | 청소 | cleaning |
| pet | 반려동물 | pet |

## 향후 계획

- [ ] 상품 연동 (쿠팡 파트너스)
- [ ] 코디 추천 모듈
- [ ] 알림 기능
- [ ] 관리자 페이지
- [ ] Android 앱

## License

Private - All rights reserved
