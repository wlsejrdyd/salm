# SALM 네이티브 앱 (Capacitor)

Spring 백엔드(salm.kr)를 그대로 WebView 에 띄우는 얇은 네이티브 래퍼. Phase B/C 의 PWA 위에 얹혀서 별도 UI 코드를 따로 안 갖는 것이 의도.

## 개발 환경 준비

```bash
# Node 18+, npm 9+ 가정
cd app
npm install
npx cap add android
npx cap add ios
```

iOS 는 macOS + Xcode 16+, CocoaPods 필요. Android 는 Android Studio + JDK 17 필요.

## 실행

```bash
# Android 에뮬레이터/기기
npm run android

# iOS 시뮬레이터
npm run ios
```

## 배포

서버(salm.kr) 코드를 푸시하면 앱 동작은 자동 반영됨 (원격 URL 모드). 네이티브 변경(플러그인 추가/아이콘 교체)이 있을 때만 스토어 재제출 필요.

### Android (Play Console)
1. `npm run open:android` → Android Studio.
2. Build > Generate Signed Bundle (.aab) → Play Console 내부 테스트 트랙 업로드.
3. 키스토어는 한 번 만들어두면 재사용. 비밀번호는 1Password 등에 보관.

### iOS (App Store / TestFlight)
1. `npm run open:ios` → Xcode.
2. Product > Archive → App Store Connect 업로드.
3. TestFlight 베타 → 심사 → 정식 배포.
4. 사전 준비: Apple Developer 계정($99/년), Bundle ID `kr.salm.app` 등록, App Store Connect 앱 생성.

## OAuth 등록 (Phase D-3)

각 콘솔에 모바일 패키지/번들 ID 등록 필요:

| 제공자 | 등록 항목 |
|--------|-----------|
| 구글 | Android: SHA-1 fingerprint + 패키지명 / iOS: bundle id |
| 카카오 | 패키지명 + 키 해시(Android), bundle id + URL scheme(iOS) |
| 네이버 | Android 패키지명, iOS bundle id |

서버는 OAuth provider 종류와 무관하게 `Authorization: Bearer <jwt>` 를 받도록 D-3 에서 갱신.

## 자산 (아이콘/스플래시)

```bash
# /assets/icon.png (1024x1024), /assets/splash.png (2732x2732) 준비 후
npm run build:assets
```

`@capacitor/assets` 가 Android/iOS 에 필요한 모든 사이즈를 자동 생성.
