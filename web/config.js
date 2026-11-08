// 추모 서버 주소. 배포할 때 docs/deploy.md 4단계처럼 바꿉니다.
// 로컬: 서버를 8080에 띄우고 web/ 폴더를 5500 같은 포트로 열면 됩니다.
window.MEMORIAL_API_BASE = window.MEMORIAL_API_BASE || (
  /^(localhost|127\.0\.0\.1)$/.test(location.hostname) ? 'http://localhost:8080' : 'https://memorial.217.142.225.154.sslip.io'
);
