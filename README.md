# 📔 Team4 Diary Application (오늘의 일기)

Spring Boot 4.0과 AWS 인프라(S3)를 활용한 사용자 맞춤형 일기(다이어리) 웹 애플리케이션입니다. 안전한 JWT 기반 인증 체계와 파일 스토리지 관리를 지원합니다.

---

## 🚀 1. 프로젝트 개요 (Overview)
- **개발 언어 및 프레임워크**: Java 17, Spring Boot 4.0.6 (`spring-boot-starter-parent`)
- **주요 기능**: 사용자 인증 및 인가, 일기 CRUD(작성·조회·수정·삭제), S3 이미지 업로드, 타임리프(Thymeleaf) 기반 프론트엔드 화면 제공
- **빌드 및 패키징 툴**: Maven (`pom.xml`)
- **애플리케이션 진입점**: `DailyApplication.java`

*(여기에 실제 서비스 구동 스크린샷이나 시연 영상 링크를 넣으면 매우 좋습니다!)*

---

## 🏗️ 2. 시스템 아키텍처 (Architecture)

Client (Browser) ↔︎ Spring MVC (Controllers) ↔︎ Service Layer ↔︎ Repository (JPA) ↔︎ MySQL
↕                                     ↕
Security (JWT)                          S3 (AWS SDK)


- **Controller**: `/api/**` 형태의 REST API 엔드포인트 및 웹 화면 컨트롤러 노출
- **Service & Repository**: Spring Data JPA를 통해 비즈니스 로직 처리 및 MySQL 데이터베이스 영속화
- **AWS S3**: 일기에 첨부되는 이미지 등의 미디어 파일을 고성능 클라우드 스토리지(Amazon S3)에 안전하게 저장 및 관리
- **Security**: Stateless 세션 정책을 기반으로, 요청마다 JWT 토큰을 검증하는 필터 체킹 수행

---

## 📂 3. 디렉토리 및 패키지 구조 (Project Structure)

| 패키지명 | 핵심 클래스/파일 | 역할 및 목적 |
|:---|:---|:---|
| `com.example.daily` | `DailyApplication` | Spring Boot 애플리케이션 부트스트랩 및 구동 |
| `config` | `SecurityConfig`, `S3Config`, `JpaConfig`, `JacksonConfig`, `WebConfig` | Spring Security, AWS SDK v2, JPA, JSON 직렬화 설정 |
| `controller` | `PreviewController`, `api/*` | 웹 화면 렌더링 및 REST API 엔드포인트 구현 |
| `domain` | `User`, `Diary` 등 Entity 클래스 | 데이터베이스 테이블과 매핑되는 JPA 엔티티 정의 |
| `dto` | Request/Response Payloads | 계층 간 데이터 교환을 위한 DTO 객체 |
| `security` | `JwtAuthenticationFilter`, Token 유틸리티 | JWT 토큰 생성, 파싱, 보안 필터 체인 제어 |
| `service` | 비즈니스 로직 비동기 구현체 | 핵심 비즈니스 로직 및 트랜잭션 관리 |
| `resources` | `application.yml`, `static/`, `templates/` | DB/S3 설정 정보, 정적 자원(CSS/JS), Thymeleaf 뷰 |

---

## 🛠️ 4. 기술 스택 및 의존성 (Tech Stack)

- **Backend core**: Spring Boot Starter (Web, Thymeleaf, Data JPA, Security, Actuator, Test)
- **Cloud Storage**: AWS SDK v2 (`software.amazon.awssdk:s3`, `sts`, `apache-client`)
- **Authentication**: JWT (`io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Database**: MySQL Connector/J (Runtime), Hibernate 7 (JPA Provider)
- **Utilities**: Lombok, Jackson Datatype JSR-310 (Java 8 날짜/시간 API 지원)

---

## ⚙️ 5. 환경 설정 및 요구사항 (Configuration)

### 필수 요구사항

* Java 17 빌드 환경
* MySQL 8.0 이상 호환 데이터베이스
* AWS S3 버킷 및 액세스 키 계정 정보

### application.yml 설정 파일 가이드

`src/main/resources/application.yml` 파일에 로컬 및 클라우드 환경 변수를 세팅해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/YOUR_DATABASE_NAME?serverTimezone=Asia/Seoul
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

aws:
  s3:
    bucket-name: YOUR_S3_BUCKET_NAME
    region: YOUR_AWS_REGION
```

---

## 💻 6. 실행 및 빌드 방법 (How to Run)

### 1) 로컬 개발 환경에서 빌드 및 패키징

프로젝트 루트 디렉토리 (`C:\CE\team4_diary\team4_app`)에서 아래 명령어를 실행합니다.

```bash
# Maven을 사용한 빌드 및 JAR 파일 생성
./mvnw clean package

# 생성된 JAR 애플리케이션 실행
java -jar target/daily-0.0.1-SNAPSHOT.jar
```

### 2) Docker 컨테이너 배포

레포지토리에 포함된 Dockerfile을 이용해 이미지 빌드 및 컨테이너 실행이 가능합니다.

```bash
docker build -t team4-diary-app .

docker run -p 8080:8080 team4-diary-app
```

---

## 🔒 7. 보안 및 인증 모델 (Security)

### Stateless 구조

JWT 기반 인증 방식을 사용하므로 서버 세션을 생성하거나 유지하지 않습니다.

```java
SessionCreationPolicy.STATELESS
```

### 인증 없이 접근 가능한 엔드포인트

* `/api/auth/**`
* `/css/**`
* `/js/**`
* `/images/**`
* `/actuator/health`

### 인증이 필요한 기능

* 일기 CRUD
* 마이페이지
* 사용자 개인 데이터 조회

요청 시 아래 헤더를 포함해야 합니다.

```http
Authorization: Bearer <JWT_TOKEN>
```

---

## 🛠️ 8. 트러블슈팅 (Troubleshooting)

### 1. 로컬 환경과 Docker 환경 간 DB 연결 실패

#### 문제

로컬 환경에서는 정상 동작하지만 Docker 컨테이너 실행 시 데이터베이스 연결 오류 발생

#### 원인

Docker 컨테이너는 독립된 네트워크 환경에서 동작하므로 localhost 설정만으로는 외부 DB에 접근할 수 없었음.

#### 해결

* 환경 변수를 이용해 DB 접속 정보 분리
* Docker 실행 시 환경 변수 주입
* application.yml 설정 외부화

#### 결과

로컬 환경과 컨테이너 환경에서 동일한 설정 구조로 운영 가능하도록 개선

---

### 2. 일기 태그(Tag) 저장 누락 문제

#### 문제

* 태그 입력 후 임시저장
* 임시저장 복원 성공
* 작성 완료 후 태그가 저장되지 않음

#### 원인

JavaScript에서 동일한 이름의 tags 변수가 전역과 지역에 중복 선언됨.

```javascript
let tags = [];

document.addEventListener('DOMContentLoaded', async () => {
    let tags = [];
});
```

#### 해결

DOMContentLoaded 내부의 중복 선언 제거

```javascript
let tags = [];

document.addEventListener('DOMContentLoaded', async () => {
    // 전역 tags 사용
});
```

#### 결과

태그 상태 관리가 일원화되었으며 저장 시 정상적으로 DB에 반영됨.

---

## 📈 9. 향후 확장 계획 (Extensibility Points)

### API 버전 관리

서비스 확장을 고려하여 `/api/v1` 기반 버전 전략 도입

### Refresh Token 도입

* Access Token 단기 운영
* Refresh Token 기반 재발급 구조 적용

### 파일 보안 강화

* 업로드 파일 확장자 검증
* MIME Type 검증
* 악성코드 검사 연동

### 테스트 커버리지 확대

* Unit Test 강화
* Integration Test 추가
* Controller 테스트 자동화
* Service 계층 테스트 보강

