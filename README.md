# 그리움의 정원 (memorial)

먼저 떠난 이를 기억하는 사람들이 함께 머무는 3D 추모 정원입니다.
입장하면 모두가 함께 쓰는 공용 추모 공간(세계수 광장)에서 시작하고, 그 안에서 고인별 개인 추모 공간으로 워프해 들어갑니다.
로그인 없이 이름만 정하고 들어옵니다.

- 세계수 광장: 여럿이 모여 촛불을 들면 든 사람 수만큼 나무가 밝아집니다.
- 개인 추모 공간: 야외 언덕(오픈월드). 누구나 찾아올 수 있고, 목록에 지금 머무는 사람 수가 보입니다.
- 사연과 편지: 공감순 전시, 댓글과 답글(1단계).
- 촛불·국화 헌화, 공간마다 따로 있는 채팅, 사람 모양 아바타(성별·나이대, 내 사진 얼굴).

## 구조

| 폴더 | 내용 |
| --- | --- |
| `server/` | Spring Boot 3.3 (Java 21), JPA + Flyway, PostgreSQL 16, STOMP 웹소켓 |
| `web/` | 빌드 없는 정적 페이지 (Three.js r128, @stomp/stompjs) |
| `deploy/` | Oracle VM 배포 스크립트 (장기 앱과 같은 VM에 함께 올림) |
| `docs/deploy.md` | 배포 방법 |

서버 구성은 장기 앱(`janggigame`)과 같습니다: Gradle Kotlin DSL, 도커 이미지는 GitHub Actions가 `ghcr.io/orinalgga/memorial-server`로 올리고, VM은 그 이미지를 받아 띄웁니다.

## 로컬에서 돌리기

```bash
./gradlew :server:bootRun          # http://localhost:8080 (DB 없이 메모리 DB로 뜸)
cd web && python3 -m http.server 5500   # http://localhost:5500
```

Postgres로 돌리려면 `docker compose up --build` (서버는 `localhost:8081`). 이때는 `web/config.js`의 로컬 주소를 8081로 바꿉니다.

테스트: `./gradlew build`

## API 요약

공간(`space`)은 `garden`(공용) 또는 개인 추모 공간 id입니다.

| | |
| --- | --- |
| `GET/POST /api/memorials`, `PUT /api/memorials/{id}` (`X-Edit-Token`), `GET /api/memorials/{id}/photo` | 개인 추모 공간 |
| `GET/POST /api/spaces/{space}/letters`, `POST /api/letters/{id}/hearts` | 사연, 공감(방문자당 1회) |
| `GET/POST /api/letters/{id}/comments` | 댓글, 답글(`parentId`) |
| `GET/POST /api/spaces/{space}/offerings` | 촛불·국화 |
| `GET/POST /api/spaces/{space}/chat` | 채팅 (최근 40개) |
| `PUT/GET/DELETE /api/avatars/{visitorId}/face` | 아바타 얼굴 사진 |
| 웹소켓 `/ws` (STOMP) | `/topic/spaces/{space}/events`, `/topic/spaces/{space}/presence`, `/topic/memorials`, `/topic/visitors`; 보내기 `/app/spaces/{space}/presence` |

## 아직 남은 일

- 로그인이 없어서 방문자 id는 브라우저마다 무작위로 만듭니다. 공감 중복은 서버에서 막지만, 마음먹고 id를 바꾸면 피할 수 있습니다.
- 신고·숨김 같은 관리 기능, 개인 추모 공간 수정 화면(서버 API는 있음).
