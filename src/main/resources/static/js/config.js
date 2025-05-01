/**
 * FreeMate - 전역 설정 관리
 * 프로젝트 공통 설정을 관리하는 파일입니다.
 */

let FreeMateConfig = {
  // API 서버 기본 URL 설정
  baseUrl: 'https://freemates.suhsaechan.me',

  // API 경로 설정
  apiPaths: {
    // 인증 관련
    auth: {
      register: '/api/auth/register',
      login: '/api/auth/login',
      refresh: '/api/auth/refresh',
      duplicateUsername: '/api/auth/duplicate/username'
    },

    // 이메일 관련
    mail: {
      send: '/api/mail/send',
      verify: '/api/mail/verify'
    }
  },

  // 토큰 설정
  token: {
    accessTokenExpiry: 60 * 60 * 1000, // 1시간 (밀리초)
    refreshTokenStorageKey: '_refresh_token' // 세션 스토리지 키
  },

  // 페이지 URL 설정
  pageUrls: {
    home: '/',
    login: '/login',
    register: '/register'
  }
};

// 전역 객체로 등록
window.FreeMateConfig = FreeMateConfig;