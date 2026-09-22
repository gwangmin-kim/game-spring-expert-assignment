# WebCraft 실시간 서버

Spring 숙련 과제로 구현한 WebCraft(마인크래프트를 웹 게임으로 만든 것) 실시간 서버. 월드 생성·저장, WebSocket 기반 실시간 이동·채팅, Redis를 이용한 접속 상태 관리와 서버 간 메시지 전달을 구현한다. 지형 생성, 인벤토리 등 게임 엔진 자체의 로직은 제공된 라이브러리(`webcraft-engine`)가 담당하고, 서버는 그 위에서 연결 관리와 API를 구현한다.

## 요구 사항

- JDK 21
- Docker (MySQL, Redis 실행용. 레벨 20부터는 애플리케이션 서버 실행에도 사용)

## 실행 방법

### 1. 환경변수 설정

`.env.example`을 복사해 `.env`를 만들고 값을 채운다.
(생략 가능 - 과제 프로젝트인 것을 고려하여 .env를 커밋에 포함시켰다.)

```bash
cp .env.example .env
```

### 2. MySQL, Redis 실행

```bash
docker compose up -d mysql redis
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows에서는 `gradlew.bat bootRun`을 사용한다. `.env`의 접속 정보는 `spring.config.import`로 자동 로드되며, 스키마는 `ddl-auto=update`로 자동 생성된다.

### 4. 접속

- REST API: `http://localhost:8080`
- WebSocket: `ws://localhost:8080/ws/worlds/{worldId}?nickname={nickname}`

### 서버 2대로 실행하기 (레벨 20)

서버 간 채팅 전파를 확인하려면 애플리케이션 서버 2대를 Docker로 함께 띄운다.

```bash
docker compose up -d --build
```

`app1`은 8081, `app2`는 8082 포트로 노출되며 둘 다 같은 MySQL, Redis를 공유한다. 각 서버는 컨테이너 내부에서 `mysql`, `redis` 서비스 이름으로 서로를 찾고, `.env`는 이미지에 포함되지 않고 실행 시점에 마운트된다.

## 레벨별 진행 내용

### 필수 항목

| 레벨 | 요구사항 | 수정 내용 |
| --- | --- | --- |
| 1 | Docker MySQL/Redis 연결, 환경변수 분리 | Docker Compose로 MySQL/Redis 실행 구성. 접속 정보를 `.env`로 분리해 `spring.config.import`로 로드. 운영용 root 계정과 별개로 `webcraft` 스키마에만 권한을 갖는 애플리케이션 전용 계정을 구성 |
| 2 | 채팅 조회 성능을 위한 인덱스 | `ChatMessage`에 `(world_id, created_at)` 복합 인덱스 추가 |
| 3 | 플레이어 등록 | 닉네임 형식 검증(Bean Validation) 추가, 등록 서비스 구현(닉네임 중복은 DB 유니크 제약 위반을 잡아 처리해 동시 등록에도 안전) |
| 4 | 월드 생성 | 월드 생성 서비스 구현, 동시 생성 요청에도 최대 개수(3개) 제한이 깨지지 않도록 처리 |
| 5 | 채팅 저장과 내역 조회 | 채팅 저장·최근 채팅 조회 서비스 메서드 구현 |
| 6 | 최근 채팅 조회 API | `GET /worlds/{worldId}/chats` 컨트롤러 매핑 |
| 7 | WebSocket 연결과 사용자 식별 | 핸드셰이크 인터셉터에서 닉네임·월드 조회 후 세션 속성에 저장하도록 구현 |
| 8 | HandshakeInterceptor 등록 | `WebSocketConfig`에 인터셉터 등록 |
| 9 | 월드별 WebSocket 세션 관리 | `WorldSessionRegistry`의 등록·조회 구현(같은 월드에서 닉네임 중복 등록 방지) |
| 10 | Redis 접속 상태 관리 | `PresenceService`의 접속 등록/해제를 Redis Sorted Set으로 구현 |
| 11 | 메시지 라우팅과 Ping/Pong | `MessageRouter`의 핸들러 위임 구현, `PingWsHandler`에서 접속 상태 갱신 및 pong 응답 구현 |
| 12 | 플레이어 이동 요청 처리 | `MoveWsHandler`에서 이동 값을 읽어 엔진에 전달 |
| 13 | 채팅 요청 처리와 응답 구성 | `ChatWsHandler`에서 채팅 내용 읽기·저장 구현, `ChatResponse` DTO 완성 |
| 14 | 같은 월드 참여자에게 채팅 전송 | `LocalChatSender`에서 `WorldBroadcaster`로 브로드캐스트 구현 |
| 15 | 접속자 목록 조회 | `OnlineUsersWsHandler`에서 열린 세션의 닉네임을 정렬해 응답, `OnlineUsersResponse` 완성 |

### 도전 항목

| 레벨 | 요구사항 | 수정 내용 |
| --- | --- | --- |
| 16 | 낙관적 락 | `WorldTrialSite.revision`에 `@Version` 적용해 동시 수정 시 하나만 커밋되도록 처리 |
| 17 | 커서 페이지 조회 | 채팅 과거 내역을 커서(생성 시각 + ID) 기반으로 페이지네이션 조회하도록 `ChatHistoryService` 구현 |
| 18 | Redis 최근 채팅 캐시 | `RecentChatCache`의 읽기/쓰기/무효화를 Redis 값과 TTL(5초)로 구현 |
| 19 | Redis Lua로 채팅 전송 횟수 제한 | Redis Lua 스크립트(`increment_chat_count.lua`)로 횟수 확인, 증가, 최초 만료 설정을 원자적으로 처리. 스크립트는 리소스 파일로 분리해 빈으로 등록하고 `ChatRateLimitService`에 주입 |
| 20 | 서버 간 채팅 전송 | 애플리케이션 이미지를 위한 `Dockerfile`(멀티스테이지 빌드) 작성, Docker Compose에 서버 2대와 healthcheck 기반 기동 순서 구성. `ChatRelay`로 Redis Pub/Sub 기반 서버 간 채팅 발행·구독 구현 |
