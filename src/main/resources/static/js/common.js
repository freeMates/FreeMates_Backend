/**
 * FreeMate - 공통 JavaScript 유틸리티 모듈
 * 인증, API 요청, 유틸리티 함수들을 포함합니다.
 */

// 전역 네임스페이스
const FreeMate = {};

/**
 * ==========================================
 * 토큰 관리 (Authentication)
 * ==========================================
 */
FreeMate.Auth = (function() {
  // 토큰을 저장할 메모리 변수 (LocalStorage 대신 메모리 사용)
  let _accessToken = null;
  let _refreshToken = null;
  let _tokenExpiry = null;

  return {
    /**
     * 로그인 응답에서 받은 토큰들을 저장합니다.
     * @param {Object} tokens - {accessToken, refreshToken} 객체
     * @param {string} nickname - 사용자 닉네임
     */
    saveTokens: function(tokens, nickname) {
      _accessToken = tokens.accessToken;
      _refreshToken = tokens.refreshToken;

      // 토큰 만료 시간 계산 (외부 설정에서 가져옴)
      _tokenExpiry = new Date().getTime() + (window.FreeMateConfig?.token?.accessTokenExpiry || 60 * 60 * 1000);

      // 세션 스토리지에 닉네임 저장 (필요시)
      sessionStorage.setItem('userNickname', nickname);

      // 인증 이벤트 발생
      document.dispatchEvent(new CustomEvent('auth:login'));
    },

    /**
     * 현재 저장된 액세스 토큰을 반환합니다.
     * @returns {string|null} 액세스 토큰 또는 null
     */
    getAccessToken: function() {
      return _accessToken;
    },

    /**
     * 현재 저장된 리프레시 토큰을 반환합니다.
     * @returns {string|null} 리프레시 토큰 또는 null
     */
    getRefreshToken: function() {
      return _refreshToken;
    },

    /**
     * 모든 토큰과 인증 관련 데이터를 제거합니다. (로그아웃)
     */
    removeTokens: function() {
      _accessToken = null;
      _refreshToken = null;
      _tokenExpiry = null;
      sessionStorage.removeItem('userNickname');

      // 로그아웃 이벤트 발생
      document.dispatchEvent(new CustomEvent('auth:logout'));
    },

    /**
     * 사용자가 현재 인증되어 있는지 확인합니다.
     * @returns {boolean} 인증 상태
     */
    isAuthenticated: function() {
      return !!_accessToken && new Date().getTime() < _tokenExpiry;
    },

    /**
     * 액세스 토큰이 만료되었을 때 리프레시 토큰으로 갱신합니다.
     * @returns {Promise} 토큰 갱신 결과 약속
     */
    refreshAccessToken: async function() {
      if (!_refreshToken) {
        return Promise.reject('No refresh token available');
      }

      try {
        const refreshPath = window.FreeMateConfig?.apiPaths?.auth?.refresh || '/api/auth/refresh';
        const response = await FreeMate.API.post(refreshPath, {
          refreshToken: _refreshToken
        }, false);

        if (response && response.accessToken) {
          _accessToken = response.accessToken;
          _tokenExpiry = new Date().getTime() + (window.FreeMateConfig?.token?.accessTokenExpiry || 60 * 60 * 1000);
          return Promise.resolve();
        } else {
          return Promise.reject('Failed to refresh token');
        }
      } catch (error) {
        // 리프레시 실패 시 로그아웃 처리
        this.removeTokens();
        return Promise.reject(error);
      }
    },

    /**
     * 현재 사용자의 닉네임을 반환합니다.
     * @returns {string|null} 사용자 닉네임 또는 null
     */
    getUserNickname: function() {
      return sessionStorage.getItem('userNickname');
    }
  };
})();

/**
 * ==========================================
 * API 요청 관리
 * ==========================================
 */
FreeMate.API = (function() {
  /**
   * API 요청의 공통 설정을 처리하는 내부 함수
   * @param {string} url - 요청 URL
   * @param {Object} options - fetch 옵션
   * @param {boolean} requiresAuth - 인증 필요 여부
   * @returns {Promise} fetch 요청 약속
   */
  const _apiRequest = async function(url, options, requiresAuth = true) {
    // 기본 헤더 설정
    options.headers = options.headers || {};
    options.headers['Content-Type'] = 'application/json';

    // 인증이 필요하고 토큰이 있으면 헤더에 추가
    if (requiresAuth && FreeMate.Auth.isAuthenticated()) {
      options.headers['Authorization'] = `Bearer ${FreeMate.Auth.getAccessToken()}`;
    }

    // URL이 절대 경로가 아니면 baseUrl을 추가
    const baseUrl = window.FreeMateConfig?.baseUrl || '';
    const fullUrl = (url.startsWith('http') ? url : baseUrl + url);

    try {
      const response = await fetch(fullUrl, options);

      // 401 에러 처리 (토큰 만료)
      if (response.status === 401 && requiresAuth) {
        try {
          // 토큰 갱신 시도
          await FreeMate.Auth.refreshAccessToken();

          // 토큰 갱신 성공 시 원래 요청 재시도
          options.headers['Authorization'] = `Bearer ${FreeMate.Auth.getAccessToken()}`;
          return _apiRequest(url, options, requiresAuth);
        } catch (refreshError) {
          // 갱신 실패 시 로그인 페이지로
          FreeMate.UI.showError('인증이 만료되었습니다. 다시 로그인해주세요.');
          window.location.href = window.FreeMateConfig?.pageUrls?.login || '/login';
          return Promise.reject(refreshError);
        }
      }

      // 응답 본문 파싱 (JSON 또는 텍스트)
      let responseData;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        responseData = await response.json();
      } else {
        responseData = await response.text();
      }

      // 성공이 아닌 경우 에러 처리
      if (!response.ok) {
        return Promise.reject({
          status: response.status,
          data: responseData
        });
      }

      return responseData;
    } catch (error) {
      console.error('API 요청 오류:', error);
      return Promise.reject(error);
    }
  };

  return {
    /**
     * GET 요청을 보냅니다.
     * @param {string} url - 요청 URL
     * @param {Object} params - URL 쿼리 파라미터
     * @param {boolean} requiresAuth - 인증 필요 여부 (기본값: true)
     * @returns {Promise} API 응답 약속
     */
    get: function(url, params = {}, requiresAuth = true) {
      // URL에 쿼리 파라미터 추가
      const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&');

      const fullUrl = queryString ? `${url}?${queryString}` : url;

      return _apiRequest(fullUrl, { method: 'GET' }, requiresAuth);
    },

    /**
     * POST 요청을 보냅니다.
     * @param {string} url - 요청 URL
     * @param {Object} data - 요청 데이터
     * @param {boolean} requiresAuth - 인증 필요 여부 (기본값: true)
     * @returns {Promise} API 응답 약속
     */
    post: function(url, data = {}, requiresAuth = true) {
      return _apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(data)
      }, requiresAuth);
    },

    /**
     * PUT 요청을 보냅니다.
     * @param {string} url - 요청 URL
     * @param {Object} data - 요청 데이터
     * @param {boolean} requiresAuth - 인증 필요 여부 (기본값: true)
     * @returns {Promise} API 응답 약속
     */
    put: function(url, data = {}, requiresAuth = true) {
      return _apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data)
      }, requiresAuth);
    },

    /**
     * DELETE 요청을 보냅니다.
     * @param {string} url - 요청 URL
     * @param {Object} data - 요청 데이터 (필요한 경우)
     * @param {boolean} requiresAuth - 인증 필요 여부 (기본값: true)
     * @returns {Promise} API 응답 약속
     */
    delete: function(url, data = {}, requiresAuth = true) {
      return _apiRequest(url, {
        method: 'DELETE',
        body: Object.keys(data).length ? JSON.stringify(data) : undefined
      }, requiresAuth);
    }
  };
})();

/**
 * ==========================================
 * UI 공통 기능
 * ==========================================
 */
FreeMate.UI = (function() {
  return {
    /**
     * 성공 메시지를 표시합니다.
     * @param {string} message - 표시할 메시지
     */
    showSuccess: function(message) {
      alert(message); // 추후 toast나 다른 UI로 대체 가능
    },

    /**
     * 오류 메시지를 표시합니다.
     * @param {string} message - 표시할 메시지
     */
    showError: function(message) {
      alert(`오류: ${message}`); // 추후 toast나 다른 UI로 대체 가능
    },

    /**
     * 로딩 인디케이터를 표시합니다.
     * @param {boolean} show - 표시 여부
     */
    showLoading: function(show = true) {
      // 로딩 인디케이터 구현 (추후 구현)
      console.log(show ? '로딩 시작' : '로딩 종료');
    },

    /**
     * 폼 입력값을 객체로 변환합니다.
     * @param {HTMLFormElement} form - 폼 엘리먼트
     * @returns {Object} 폼 데이터 객체
     */
    getFormData: function(form) {
      const formData = new FormData(form);
      const data = {};

      for (const [key, value] of formData.entries()) {
        data[key] = value;
      }

      return data;
    }
  };
})();

/**
 * ==========================================
 * 유틸리티 함수
 * ==========================================
 */
FreeMate.Util = (function() {
  return {
    /**
     * 이메일 형식이 유효한지 검증합니다.
     * @param {string} email - 검증할 이메일 문자열
     * @returns {boolean} 유효 여부
     */
    validateEmail: function(email) {
      const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(String(email).toLowerCase());
    },

    /**
     * 패스워드가 요구사항을 만족하는지 검증합니다.
     * @param {string} password - 검증할 패스워드
     * @returns {boolean} 유효 여부
     */
    validatePassword: function(password) {
      // 최소 6자 이상
      return password.length >= 6;
    },

    /**
     * 날짜를 지정된 형식으로 포맷팅합니다.
     * @param {Date} date - 포맷팅할 날짜
     * @param {string} format - 형식 (예: 'YYYY-MM-DD')
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate: function(date, format = 'YYYY-MM-DD') {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');

      return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day);
    },

    /**
     * 에러 응답에서 에러 메시지를 추출합니다.
     * @param {Object} error - API 에러 객체
     * @returns {string} 사용자에게 표시할 에러 메시지
     */
    getErrorMessage: function(error) {
      if (error && error.data && error.data.message) {
        return error.data.message;
      } else if (error && error.message) {
        return error.message;
      } else {
        return '알 수 없는 오류가 발생했습니다.';
      }
    },

    /**
     * 현재 나이를 계산합니다.
     * @param {number} birthYear - 출생 연도
     * @returns {number} 만 나이
     */
    calculateAge: function(birthYear) {
      return new Date().getFullYear() - birthYear;
    }
  };
})();

// 전역 객체로 등록
window.FreeMate = FreeMate;