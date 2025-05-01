/**
 * FreeMate - 회원가입 관련 서비스
 * 회원가입 및 이메일 인증 관련 API 호출 처리
 */

let RegisterService = {
  /**
   * 이메일 인증 메일을 발송합니다.
   * @param {string} email - 인증할 이메일 주소
   * @returns {Promise} API 응답 약속
   */
  sendVerificationEmail: function(email) {
    const url = window.FreeMateConfig?.apiPaths?.mail?.send || '/api/mail/send';

    FreeMate.UI.showLoading(true);

    return FreeMate.API.get(url, { mail: email }, false)
    .then(response => {
      FreeMate.UI.showSuccess('인증메일이 발송되었습니다. 이메일을 확인해주세요.');
      return response;
    })
    .catch(error => {
      const errorMessage = FreeMate.Util.getErrorMessage(error);

      if (error.status === 400 && errorMessage.includes('DUPLICATE_EMAIL')) {
        FreeMate.UI.showError('이미 가입된 이메일입니다.');
      } else if (error.status === 400 && errorMessage.includes('INVALID_EMAIL')) {
        FreeMate.UI.showError('유효하지 않은 이메일 주소입니다.');
      } else {
        FreeMate.UI.showError('이메일 발송 중 오류가 발생했습니다.');
      }

      throw error;
    })
    .finally(() => {
      FreeMate.UI.showLoading(false);
    });
  },

  /**
   * 이메일 인증 상태를 확인합니다.
   * 실제로 이 API는 백엔드에는 없지만 프론트에서 동작 방식을 보여주기 위해 추가합니다.
   * 실제 인증은 메일의 링크를 클릭하면 /api/mail/verify로 자동 리다이렉션됩니다.
   *
   * @param {string} email - 확인할 이메일 주소
   * @returns {Promise<boolean>} 인증 성공 여부
   */
  checkVerificationStatus: function(email) {
    // 자동 인증 로직이기 때문에 세션 스토리지를 통해 인증 상태를 확인하는 방식을 구현
    // 실제로는 백엔드에 확인 API가 있어야 더 정확함
    const verifiedEmails = JSON.parse(sessionStorage.getItem('verifiedEmails') || '{}');
    return Promise.resolve(verifiedEmails[email] === true);
  },

  /**
   * 아이디 중복을 확인합니다.
   * @param {string} username - 확인할 아이디
   * @returns {Promise<boolean>} 중복 여부 (true면 중복)
   */
  checkUsernameDuplicate: function(username) {
    const url = window.FreeMateConfig?.apiPaths?.auth?.duplicateUsername || '/api/auth/duplicate/username';

    return FreeMate.API.get(url, { username: username }, false)
    .then(response => {
      return response; // 백엔드 응답: true면 중복, false면 사용 가능
    })
    .catch(error => {
      FreeMate.UI.showError('아이디 중복 확인 중 오류가 발생했습니다.');
      // 에러 시에는 안전하게 중복으로 처리
      return true;
    });
  },

  /**
   * 회원가입을 완료합니다.
   * @param {Object} userData - 회원가입에 필요한 사용자 데이터
   * @returns {Promise} API 응답 약속
   */
  register: function(userData) {
    const url = window.FreeMateConfig?.apiPaths?.auth?.register || '/api/auth/register';

    // API 명세에 맞게 데이터 변환
    const requestData = {
      username: userData.username,
      password: userData.password,
      nickname: userData.nickname,
      email: userData.email,
      gender: userData.gender === 'male' ? 'MALE' : 'FEMALE',
      age: FreeMate.Util.calculateAge(userData.birthYear)
    };

    FreeMate.UI.showLoading(true);

    return FreeMate.API.post(url, requestData, false)
    .then(response => {
      FreeMate.UI.showSuccess('회원가입이 완료되었습니다!');
      return response;
    })
    .catch(error => {
      const errorMessage = FreeMate.Util.getErrorMessage(error);

      if (error.status === 400 && errorMessage.includes('DUPLICATE_NICKNAME')) {
        FreeMate.UI.showError('이미 사용 중인 닉네임입니다.');
      } else if (error.status === 400 && errorMessage.includes('INVALID_AGE')) {
        FreeMate.UI.showError('유효하지 않은 나이입니다. 18세 이상 90세 이하만 가입 가능합니다.');
      } else if (error.status === 400 && errorMessage.includes('DUPLICATE_USERNAME')) {
        FreeMate.UI.showError('이미 사용 중인 아이디입니다.');
      } else {
        FreeMate.UI.showError('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
      }

      throw error;
    })
    .finally(() => {
      FreeMate.UI.showLoading(false);
    });
  }
};

// 전역 객체에 추가
window.RegisterService = RegisterService;