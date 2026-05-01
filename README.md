# 🏠 SALM - 살림 정보 공유 플랫폼

> 일상 살림 정보를 숏폼 영상으로 공유 플랫폼

[![Deploy](https://github.com/wlsejrdyd/salm/actions/workflows/deploy.yml/badge.svg)](https://github.com/wlsejrdyd/salm/actions/workflows/deploy.yml)

## 🌐 Live

- **메인**: https://salm.kr
- **숏폼 피드**: https://salm.kr/feed
- **옷장 관리**: https://salm.kr/closet

---

## ✨ 주요 기능

### 📹 영상 피드
- 숏폼 스타일 살림 정보 영상 공유
- **HLS 적응형 스트리밍** (360p / 720p / 1080p, hls.js)
- 폴백 progressive MP4 (HLS 미지원 환경)
- **비동기 인코딩** — 업로드 즉시 응답, 백그라운드에서 ffmpeg 처리, 상태 머신 (`UPLOADED` → `PROCESSING` → `READY` / `FAILED`)
- TikTok 스타일 세로 스와이프 피드 (`/feed`, scroll-snap-y)

### 👕 옷장 관리 (Closet)
- 내 옷장 등록 및 관리
- 현재 날씨 기반 코디 추천
- 아바타로 코디 미리보기
- ~~closet.salm.kr~~ → salm.kr/closet 으로 통합

### 👤 회원
- OAuth 2.0 소셜 로그인 (구글) — 카카오/네이버는 모바일 앱과 함께 추가 예정 (Phase D)
- 세션 기반 웹 인증 + JWT 기반 모바일 앱 인증

### 📱 모바일 / 앱
- **PWA**: manifest, service worker, 홈 화면에 추가, 오프라인 셸 캐시
- **네이티브 앱**: Capacitor 래퍼 (`app/`) — Android + iOS

---

## 🛠️ Tech Stack

| 영역 | 기술 |
|------|------|
| **Backend** | Spring Boot 3.2, Spring Security, JPA |
| **Database** | MariaDB |
| **Frontend** | Thymeleaf, Vanilla JS, hls.js |
| **Media** | FFmpeg (HLS ABR 인코딩) |
| **Server** | Nginx (리버스 프록시 + `/media/` 정적 서빙) |
| **PWA** | Web App Manifest, Service Worker |
| **Native** | Capacitor 6 (Android + iOS) |
| **Infra** | Rocky Linux 9, Systemd |
| **CI/CD** | GitHub Actions (자동 백업 + 헬스체크 + 자동 롤백) |
| **Monitoring** | Spring Actuator, Prometheus + Grafana ([infra.deok.kr](https://infra.deok.kr)) |

---

## 📁 프로젝트 구조

```
salm/
├── src/main/java/kr/salm/
│   ├── config/          # Security, Web, ResourceHandler 설정
│   ├── community/       # 영상 + 댓글 + 좋아요 + 북마크
│   │   ├── controller/
│   │   ├── service/     # VideoService(업로드 흐름) + VideoEncodeListener(비동기 인코딩)
│   │   ├── entity/      # Video.Status enum
│   │   └── ...
│   ├── closet/          # 옷장
│   ├── auth/            # 회원 + OAuth2
│   ├── file/            # VideoFileService (FFmpeg HLS 파이프라인)
│   └── core/            # 공통 (예외, DTO, 보안 유틸)
├── src/main/resources/
│   ├── static/
│   │   ├── manifest.webmanifest
│   │   ├── sw.js
│   │   └── ...
│   ├── templates/
│   │   ├── community/
│   │   │   ├── feed.html         # 세로 스와이프 숏폼 피드
│   │   │   ├── video-detail.html # hls.js 플레이어
│   │   │   └── ...
│   │   └── ...
│   └── application.yml
├── app/                          # Capacitor 네이티브 앱 모듈
│   ├── capacitor.config.ts
│   ├── package.json
│   └── README.md
└── .github/workflows/deploy.yml  # 자동 배포 + 자동 롤백
```

---

## 🎬 영상 처리 파이프라인

```
업로드 (multipart) ─┐
                    ├─→ Spring 검증/원본 저장/메타 추출 (~수초, 동기)
                    │     └─ Video status=PROCESSING 으로 DB 저장 → 사용자에게 즉시 응답
                    │
                    └─→ 트랜잭션 커밋 후 EncodeRequestedEvent
                          └─→ VideoEncodeListener 가 ffmpeg 워커풀에 큐잉
                                └─→ HLS ABR (360p/720p/[1080p]) + master.m3u8 + progressive.mp4(폴백)
                                      └─ Video status=READY (또는 FAILED + reason)

재생: 클라이언트(hls.js) ─→ Nginx /media/... ─→ Disk
       (Spring 미경유 → 톰캣 스레드 보호)
```

### Nginx 설정 예 (서버 측)

```nginx
location /media/ {
    alias /app/salm/shared/uploads/;
    add_header Cache-Control "public, max-age=31536000, immutable";
    add_header Accept-Ranges bytes;
    sendfile on;
    tcp_nopush on;
    aio threads;
}
```

레거시 `/videos/`, `/thumbnails/`, `/clothes/` 도 동일 패턴으로 alias 권장. 미설정 환경에서는 Spring `WebMvcConfig` 의 ResourceHandler 가 폴백.

---

## 🚀 배포

### 자동 배포 (GitHub Actions)
`main` 브랜치 push 시:
1. 배포 전 자동 백업 (`/app/backups/salm/salm-YYYYMMDD-HHMMSS.tar.gz`, 최근 5개 보존)
2. SSH → `git pull` + `./gradlew build -x test` + `systemctl restart salm`
3. `/actuator/health` 헬스체크 (15초 간격, 6회 재시도)
4. 실패 시 가장 최근 백업으로 자동 롤백 + 재시작 + 재헬스체크

### 수동 배포
```bash
cd /app/salm/salm
git pull origin main
./gradlew build -x test
sudo systemctl restart salm
```

---

## 🧪 검증 체크리스트

- [ ] `curl https://salm.kr/actuator/health` → `{"status":"UP"}`
- [ ] 영상 업로드 시 응답 ≤ 2초 (즉시 PROCESSING 상태)
- [ ] DevTools Network: `master.m3u8` → `360p/seg_*.ts` 순 호출, `Server: nginx`
- [ ] Slow 3G 시뮬레이션에서 끊김 없이 360p로 자동 다운시프트
- [ ] Lighthouse PWA 점수 90+
- [ ] `/feed` 세로 스와이프, IntersectionObserver로 화면 밖 영상 자동 정지

---

## 👤 Author

- GitHub: [@wlsejrdyd](https://github.com/wlsejrdyd)
- Email: wlsejrdyd@gmail.com
