# 배포: 장기 앱과 같은 Oracle VM에 함께 올리기

추모 앱은 장기 앱(`orinalgga/janggigame`)이 돌고 있는 Oracle Cloud VM에 같이 올라갑니다.
구성은 장기 앱과 같습니다.

| 부분 | 어디서 | 비고 |
| --- | --- | --- |
| 서버 (Spring Boot) | Oracle VM의 도커 | GitHub Actions가 만든 `ghcr.io/orinalgga/memorial-server` 이미지를 받아 씀 |
| DB (PostgreSQL 16) | 같은 VM의 도커 | 장기 앱 DB와 따로 둠 |
| HTTPS | 장기 앱의 caddy | `memorial.<공인 IP>.sslip.io` 로 인증서를 받아 추모 서버로 넘김 |
| 웹 (`web/`) | Cloudflare Pages | 빌드 없는 정적 파일 |

비밀값(DB 비밀번호 등)은 VM 안에서 만들고, 채팅이나 저장소에 올리지 않습니다.

## 1. 서버 이미지 공개

`main`에 머지되면 `Server image` 워크플로가 `ghcr.io/orinalgga/memorial-server:latest`를 올립니다.
처음 한 번은 GitHub의 Packages → `memorial-server` → Package settings에서 공개(Public)로 바꿔 둡니다.
(장기 앱 이미지와 같은 방법입니다. 비공개로 두려면 VM에서 `docker login ghcr.io`가 필요합니다.)

## 2. VM에서 추모 서버 띄우기

```bash
ssh ubuntu@<공인 IP>
git clone https://github.com/orinalgga/memorial.git ~/memorial
cd ~/memorial
bash deploy/setup-on-janggi-vm.sh https://<Pages 프로젝트>.pages.dev
```

이 스크립트가 하는 일:

- `.env`를 만듭니다. `COMPOSE_FILE`에 `deploy/compose.janggi.yml`이 들어가서, 서버가 장기 앱 도커 네트워크(`janggigame_default`)에 `memorial-server`라는 이름으로 붙습니다.
- DB 비밀번호를 무작위로 만듭니다.
- 이미지를 받아 서버와 DB를 띄웁니다. VM 안에서는 `127.0.0.1:8081`로 열립니다(장기 앱이 8080).
- 5분마다 새 버전을 받는 자동 업데이트를 cron에 등록합니다. 로그는 `~/memorial-auto-update.log`.

확인: `curl -s localhost:8081/actuator/health` → `{"status":"UP"}`

## 3. 장기 앱 caddy에 추모 주소 더하기

장기 앱 저장소에서 두 군데를 고칩니다(같은 내용이 `deploy/Caddyfile.janggi-snippet`에 있습니다).

`janggigame/deploy/Caddyfile` 끝에:

```
{$MEMORIAL_API_HOST} {
	encode gzip
	reverse_proxy memorial-server:8080
}
```

`janggigame/docker-compose.yml`의 caddy `environment`에:

```yaml
      MEMORIAL_API_HOST: ${MEMORIAL_API_HOST:-memorial.localhost}
```

그리고 VM의 `~/janggigame/.env`에:

```
MEMORIAL_API_HOST=memorial.<공인 IP>.sslip.io
```

장기 앱 쪽 변경이 main에 머지되면 장기 앱 자동 업데이트가 caddy를 다시 띄웁니다. 바로 하려면 `cd ~/janggigame && docker compose up -d caddy`.

확인: `curl -s https://memorial.<공인 IP>.sslip.io/actuator/health`

> 주의: 장기 앱의 `deploy/install-auto-update.sh`는 `deploy/auto-update.sh`가 들어간 cron 줄을 모두 지우고 자기 줄을 넣습니다.
> 장기 앱 쪽에서 그 스크립트를 다시 돌렸다면 `bash ~/memorial/deploy/install-auto-update.sh`도 한 번 더 돌려 주세요.

## 4. 웹을 Cloudflare Pages에 올리기

1. Cloudflare Pages → Create → Connect to Git → `orinalgga/memorial`
2. Build command: 비움, Build output directory: `web`
3. `web/config.js`의 `MEMORIAL_API_BASE`를 `https://memorial.<공인 IP>.sslip.io`로 바꿔 커밋합니다.
4. Pages 주소가 2단계에서 넣은 주소와 다르면 VM의 `~/memorial/.env`에서 `MEMORIAL_CORS_ALLOWED_ORIGINS`를 고치고 `docker compose up -d`.

## 메모리

장기 앱 VM(A1, 6GB)에서 추모 서버는 `-Xmx256m` + Postgres로 대략 500MB 안쪽을 씁니다.
1GB짜리 VM이라면 스왑(장기 앱 `setup-vm.sh`가 만듦)이 있어야 합니다.

## 손으로 업데이트

```bash
cd ~/memorial && bash deploy/update.sh
```
