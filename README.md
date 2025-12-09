# 🏠 SALM - 살림 정보 공유 플랫폼

> 일상 살림 정보를 숏폼 영상으로 공유 플랫폼

[![Deploy](https://github.com/wlsejrdyd/salm/actions/workflows/deploy.yml/badge.svg)](https://github.com/wlsejrdyd/salm/actions/workflows/deploy.yml)

## 🌐 Live

- **메인**: https://salm.kr
- **옷장 관리**: https://salm.kr/closet

---

## ✨ 주요 기능

### 📹 영상 피드
- 숏폼 스타일 살림 정보 영상 공유
- FFmpeg 자동 인코딩 (4K → 1080p, 최적화 압축)
- Nginx 다이렉트 서빙 (빠른 스트리밍)

### 👕 옷장 관리 (Closet)
- 내 옷장 등록 및 관리
- 현재 날씨 기반 코디 추천
- 아바타로 코디 미리보기
- ~~closet.salm.kr~~ → salm.kr/closet 으로 통합

### 👤 회원
- OAuth 2.0 소셜 로그인 (카카오, 네이버, 구글)
- JWT 기반 인증

---

## 🛠️ Tech Stack

| 영역 | 기술 |
|------|------|
| **Backend** | Spring Boot 3.x, Spring Security, JPA |
| **Database** | MariaDB |
| **Frontend** | Thymeleaf, Vanilla JS |
| **Media** | FFmpeg (영상 인코딩) |
| **Server** | Nginx (리버스 프록시 + 정적 파일 서빙) |
| **Infra** | Rocky Linux 9, Systemd |
| **CI/CD** | GitHub Actions |
| **Monitoring** | Prometheus + Grafana ([infra.deok.kr](https://infra.deok.kr)) |

---

## 📁 프로젝트 구조

```
salm/
├── src/main/java/kr/salm/
│   ├── config/          # Security, Web 설정
│   ├── controller/      # API & Web 컨트롤러
│   ├── domain/          # Entity
│   ├── dto/             # Request/Response DTO
│   ├── repository/      # JPA Repository
│   ├── service/         # 비즈니스 로직
│   └── util/            # 유틸리티 (파일, 인코딩 등)
├── src/main/resources/
│   ├── static/          # CSS, JS, 이미지
│   ├── templates/       # Thymeleaf 템플릿
│   └── application.yml  # 설정 (환경변수 참조)
└── .github/workflows/   # CI/CD
```

---

## 🚀 배포

### 자동 배포 (GitHub Actions)
`main` 브랜치 push 시 자동 배포:
1. 배포 전 자동 백업
2. Git pull + Gradle build
3. 서비스 재시작
4. 헬스체크 (실패 시 자동 롤백)

### 수동 배포
```bash
cd /app/salm/salm
git pull origin main
./gradlew build -x test
sudo systemctl restart salm
```

---

## 🔗 연관 프로젝트

| 프로젝트 | 설명 |
|---------|------|
| [infra](https://github.com/wlsejrdyd/infra) | 인프라 모니터링 대시보드 |
| [mgmt](https://github.com/wlsejrdyd/mgmt) | 통합 관리 시스템 |

---

## 👤 Author

- GitHub: [@wlsejrdyd](https://github.com/wlsejrdyd)
- Email: wlsejrdyd@gmail.com
