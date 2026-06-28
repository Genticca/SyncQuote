# SyncQuote API

SyncQuote API는 SyncQuote 서비스의 백엔드 애플리케이션입니다. 현재 저장소는 Spring Boot 기반 API 서버 골격, 개발용 MySQL/Redis Docker Compose 구성, MyBatis 기본 설정을 포함합니다.

프런트엔드는 별도 Git 저장소에서 Vue.js로 개발하며, 이 저장소의 Docker Compose에는 프런트엔드 컨테이너를 포함하지 않습니다.

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
.
├── Dockerfile
├── compose.yaml
├── build.gradle
├── settings.gradle
└── src
    ├── main
    │   ├── java/com/syncquote
    │   │   ├── SyncquoteApiApplication.java
    │   │   └── mapper/SyncquoteMapper.java
    │   └── resources
    │       ├── application.yaml
    │       └── mappers/
    └── test
        ├── java/com/syncquote/SyncquoteApiApplicationTests.java
        └── resources/application.yaml
```

## 현재 골격

### Spring Boot 애플리케이션

`SyncquoteApiApplication`이 애플리케이션 진입점입니다. `@MapperScan("com.syncquote.mapper")`를 통해 MyBatis Mapper 인터페이스를 스캔합니다.

### MyBatis

기본 설정은 `src/main/resources/application.yaml`에 있습니다.

- XML Mapper 위치: `classpath:/mappers/**/*.xml`
- 타입 별칭 패키지: `com.syncquote`
- DB 컬럼의 snake_case를 Java camelCase로 매핑
- 현재 Mapper는 빈 인터페이스 골격만 존재합니다.

### Docker Compose

`compose.yaml`은 개발용 인프라를 제공합니다.

- MySQL 8.4
- Redis 7.4 Alpine
- API 컨테이너는 `app` 프로필로 선택 실행

Spring Boot Docker Compose 지원이 활성화되어 있어 로컬에서 `bootRun`으로 실행하면 Compose 기반 개발 서비스를 함께 사용할 수 있습니다.

## 실행 방법

### 로컬 개발 실행

```bash
./gradlew bootRun
```

### MySQL/Redis만 직접 실행

```bash
docker compose up -d mysql redis
```

### API까지 컨테이너로 실행

```bash
docker compose --profile app up --build
```

API 컨테이너는 내부 Compose 네트워크에서 MySQL과 Redis에 연결합니다.

## 테스트

```bash
./gradlew test
```

테스트 환경에서는 Docker Compose를 비활성화하고 H2 인메모리 DB를 사용합니다.

## 개발 메모

- 데이터베이스 이름: `syncquote`
- 개발용 MySQL 사용자: `syncquote`
- 개발용 Redis 포트: `6379`
- 백엔드 기본 포트: `8080`
- 운영 또는 배포 환경에서는 Compose의 기본 비밀번호를 그대로 사용하지 않아야 합니다.
