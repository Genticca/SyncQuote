# SyncQuote API

SyncQuote API는 SyncQuote 서비스의 Spring Boot 백엔드 애플리케이션입니다. 현재 저장소는 MySQL/Redis 기반 MVP 개발을 위한 API 서버 골격, Docker Compose 개발 인프라, MyBatis 기본 설정을 포함합니다.

프런트엔드와 인프라는 별도 Git 저장소로 관리합니다. 로컬에 `SyncQuote_Front/`, `SyncQuote_Infra/` 디렉터리를 둘 수 있지만 이 백엔드 저장소에는 포함하지 않습니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- Spring Validation
- Spring Actuator
- Spring Data Redis
- MyBatis Spring Boot Starter 4.0.1
- MySQL Connector/J
- Gradle 9.5.1 Wrapper
- Docker Compose

## 프로젝트 구조

```text
src/main/java/com/syncquote
├── SyncquoteApiApplication.java
├── global/      # 설정, 예외, 공통 응답
├── common/      # Redis Key, SSE 공통 유틸
├── exchange/    # Upbit/Binance 외부 API 연동 골격
├── market/      # market_info + markets:{exchange}
├── candle/      # candles + candles:1s:{exchange}:{code}
└── ticker/      # ticker_latest + ticker/ticker:rest Redis 구조
```

MyBatis XML은 도메인별로 분리합니다.

```text
src/main/resources/mappers
├── market/MarketMapper.xml
├── candle/CandleMapper.xml
└── ticker/TickerMapper.xml
```

현재는 MVP 초기 골격만 잡아둔 상태라 각 클래스 내부 로직은 비어 있습니다.

## 도메인 구조

### `global`

Spring, Redis, MyBatis, SSE, Web 설정과 전역 예외/응답 구조를 둘 위치입니다.

### `common`

여러 도메인에서 공유하는 Redis Key 생성기와 SSE 발행/Emitter 관리 구조를 둡니다.

### `exchange`

Upbit/Binance REST API 응답 DTO, Client, 거래소 코드 정규화 구조를 둡니다. 외부 API 원본 응답을 내부 도메인 모델로 변환하는 책임을 가질 예정입니다.

### `market`

MySQL `market_info`와 Redis `markets:{exchange}`를 담당합니다.

### `candle`

MySQL `candles`와 Redis `candles:1s:{exchange}:{code}`를 담당합니다. 1초봉 임시 저장, SSE 송출, 1분봉 집계 골격을 포함합니다.

### `ticker`

MySQL `ticker_latest`, Redis Stream `ticker:{exchange}:{code}`, Redis Hash `ticker:rest:{exchange}:{code}`를 담당합니다. 실시간 현재가 송출과 최신값 갱신 골격을 포함합니다.

## 현재 Schema 기준

MySQL 영구 저장 대상은 다음 3개입니다.

| 도메인 | 테이블 | 설명 |
| --- | --- | --- |
| `market` | `market_info` | 거래소별 마켓/페어 기준 정보 |
| `candle` | `candles` | 차트용 OHLCV 봉 데이터 |
| `ticker` | `ticker_latest` | 종목별 최신 현재가 스냅샷 |

Redis 사용 대상은 다음 4개입니다.

| 도메인 | Redis Key | Type | 설명 |
| --- | --- | --- | --- |
| `market` | `markets:{exchange}` | Hash | 거래소별 마켓 정보 캐시 |
| `candle` | `candles:1s:{exchange}:{code}` | List | 1초봉 임시 저장 |
| `ticker` | `ticker:{exchange}:{code}` | Stream | 실시간 현재가 버퍼 |
| `ticker` | `ticker:rest:{exchange}:{code}` | Hash | REST 보조 메타 캐시 |

`favorite`는 프런트엔드 Cookie로 처리하므로 백엔드 도메인에서 제외합니다. `ticker_realtime`, `ticker_rest_meta`는 현재 MySQL 영구 저장 대상에서 제외합니다.

## MyBatis

기본 설정은 `src/main/resources/application.yaml`에 있습니다.

- Mapper XML 위치: `classpath:/mappers/**/*.xml`
- 타입 별칭 패키지: `com.syncquote`
- DB 컬럼의 snake_case를 Java camelCase로 매핑
- Mapper 인터페이스 스캔 대상:
  - `com.syncquote.market.mapper`
  - `com.syncquote.candle.mapper`
  - `com.syncquote.ticker.mapper`

현재 XML Mapper는 네임스페이스만 연결된 빈 골격입니다.

## DB 스키마 관리

현재 의존성에는 JPA, Flyway, Liquibase가 없으므로 애플리케이션 시작 시 DB 스키마를 자동 생성하지 않습니다.

MVP 단계에서 빠르게 시작하려면 `schema.sql`을 둘 수 있지만, Docker/K8s 배포까지 고려하면 Flyway 같은 마이그레이션 도구를 도입하는 방향이 안전합니다.

```text
MVP 초기: schema.sql 또는 수동 DDL
운영 준비: Flyway migration 도입
Docker/K8s: 환경변수, ConfigMap, Secret으로 DB/Redis 연결 정보 주입
```

## Docker Compose

`compose.yaml`은 개발용 인프라를 제공합니다.

- MySQL 8.4
- Redis 7.4 Alpine
- API 컨테이너는 `app` 프로필로 선택 실행
- `01-schema.sql`은 MySQL 컨테이너의 `/docker-entrypoint-initdb.d/01-schema.sql`로 마운트되어 최초 DB 초기화 시 실행

Spring Boot Docker Compose 지원이 활성화되어 있어 로컬에서 `bootRun`으로 실행하면 Compose 기반 개발 서비스를 함께 사용할 수 있습니다.

운영 또는 K8s 환경에서는 `prod` 프로파일을 사용하고 Docker Compose 자동 실행을 끈 뒤 외부 인프라에 연결합니다.

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DOCKER_COMPOSE_ENABLED=false
```

## Profile 구성

기본 프로파일은 `develop`입니다.

- `application.yaml`: 공통 설정, 기본 프로파일, MyBatis 설정
- `application-develop.yaml`: 로컬 개발용 MySQL/Redis 연결, Docker Compose 활성화
- `application-prod.yaml`: 배포용 외부 MySQL/Redis 연결, Docker Compose 비활성화

배포 환경에서는 아래 환경변수를 주입해야 합니다.

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
SPRING_DATA_REDIS_HOST=...
SPRING_DATA_REDIS_PORT=6379
```

## 실행 방법

### 로컬 개발 실행

```bash
./gradlew bootRun
```

명시적으로 개발 프로파일을 지정하려면 아래처럼 실행합니다.

```bash
SPRING_PROFILES_ACTIVE=develop ./gradlew bootRun
```

### MySQL/Redis만 직접 실행

```bash
docker compose up -d mysql redis
```

`01-schema.sql`은 MySQL 데이터 볼륨이 비어 있는 최초 초기화 시점에만 실행됩니다. 이미 `mysql-data` 볼륨이 만들어진 뒤 SQL을 다시 적용하려면 볼륨을 지우고 다시 띄우거나 MySQL에 직접 접속해 SQL을 실행해야 합니다.

### API까지 컨테이너로 실행

```bash
docker compose --profile app up --build
```

## 테스트

```bash
./gradlew test
```

테스트 환경에서는 Docker Compose를 비활성화하고 H2 인메모리 DB를 사용합니다.

## 로컬 전용 파일

아래 항목은 백엔드 저장소에 포함하지 않습니다.

- `SyncQuote_Front/`: 별도 Git 저장소로 관리하는 프런트엔드 프로젝트
- `SyncQuote_Infra/`: 별도 Git 저장소로 관리하는 인프라 프로젝트
- `DB_정리_시트_정리본.xlsx`: 로컬 DB 설계 참고 문서

## 개발 메모

- 데이터베이스 이름: `syncquote`
- 개발용 MySQL 사용자: `syncquote`
- 개발용 Redis 포트: `6379`
- 백엔드 기본 포트: `8080`
- 운영 또는 배포 환경에서는 Compose의 기본 비밀번호를 그대로 사용하지 않아야 합니다.
