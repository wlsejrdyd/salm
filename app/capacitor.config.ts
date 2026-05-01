import type { CapacitorConfig } from '@capacitor/cli';

/**
 * 원격 URL 모드:
 *   - 서버(salm.kr)를 그대로 WebView 에 로드.
 *   - 배포 한 번이면 앱도 즉시 갱신.
 *   - 오프라인 셸이 필요해지면 webDir 로 정적 빌드를 떨어뜨리고 server.url 을 제거.
 */
const config: CapacitorConfig = {
  appId: 'kr.salm.app',
  appName: 'SALM',
  webDir: 'public',
  server: {
    url: 'https://salm.kr',
    androidScheme: 'https',
    cleartext: false,
    allowNavigation: ['salm.kr', '*.salm.kr', 'kauth.kakao.com', 'kapi.kakao.com', 'nid.naver.com', 'accounts.google.com']
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 1500,
      backgroundColor: '#10b981',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert']
    }
  }
};

export default config;
