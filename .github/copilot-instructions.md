# Copilot Instructions

## Project Overview
Java Spring Boot chat backend named **Jace**. Exposes REST APIs and a real-time WebSocket (STOMP/SockJS) layer. Stack: Spring Boot, PostgreSQL, Redis, Flyway, JWT auth. Optional RabbitMQ for WebSocket broker scaling.

---

## Architecture

```
controller/          REST endpoints (one controller per resource)
controller/ws/       STOMP WebSocket handlers (@MessageMapping)
service/             Business logic; all transactional work lives here
domain/              JPA entities (Chat, Message, User, Participant, …)
dto/                 Request/response objects and common wrappers
security/            JWT filter, STOMP auth interceptor, SecurityUtils
repository/          Spring Data JPA repositories
web/                 GlobalExceptionHandler
events/              Spring ApplicationEvent listeners (presence, WS lifecycle)
config/              SecurityConfig, WebSocketConfig, etc.
```

DB schema managed by Flyway: `src/main/resources/db/migration/V{n}__description.sql`. Add new migrations as `V{next}__description.sql`; never edit existing ones.

---

## Critical Patterns

### 1. All REST responses use `ResponseFactory`
Never return `ResponseEntity` directly. Always use the static helpers:
```java
return ResponseFactory.success(dto, "Lấy thành công");
return ResponseFactory.created(dto, "Tạo thành công");
return ResponseFactory.noContent();
return ResponseFactory.notFound("Không tìm thấy");
```
Response messages are written in **Vietnamese**.

### 2. Current user identity
```java
UUID me = SecurityUtils.currentUserIdOrThrow(); // throws if unauthenticated
UUID me = SecurityUtils.getCurrentUserId();      // same, alternate alias
```
The JWT subject is the user's UUID (string). `JwtAuthenticationFilter` sets it in `SecurityContextHolder`; `StompAuthChannelInterceptor` mirrors this for WebSocket sessions.

### 3. Domain entities
All entities use `@Data @Builder @NoArgsConstructor @AllArgsConstructor` (Lombok) and UUID PKs via `@UuidGenerator`. Example: `domain/Chat.java`, `domain/Message.java`.

### 4. WebSocket messaging
- STOMP endpoint: `/ws` (SockJS)
- App destination prefix: `/app` → handled by `@MessageMapping` in `controller/ws/`
- Broker topics: `/topic/{chatId}`, `/queue/{userId}`, `/user/…`
- Auth: JWT token sent in STOMP CONNECT header; validated by `StompAuthChannelInterceptor`
- Broker mode: `embedded` (default) or `rabbit` — set via `app.ws.broker=rabbit`

### 5. File storage
Files saved to `./uploads` (configurable via `FILE_STORAGE_PATH`). URL pattern: `{baseUrl}/api/v1/files/raw/{storedName}`. `/api/v1/files/raw/**` is public (no auth required).

---

## Key Configuration (application.yml)
All values are environment-variable-driven with dev defaults:

| Config | Env Var | Default |
|---|---|---|
| DB URL | `DB_URL` | `jdbc:postgresql://localhost:5432/chatdb` |
| Redis | `REDIS_HOST` / `REDIS_PORT` | `localhost:6379` |
| JWT secret | `JWT_SECRET` | (base64 dev key) |
| File storage path | `FILE_STORAGE_PATH` | `./uploads` |
| Profile | `ACTIVE_PROFILE` | `stg` |

---

## Build & Run
```bash
# Run locally (requires Postgres + Redis running)
./mvnw spring-boot:run

# Build jar
./mvnw clean package -DskipTests

# Reset DB (runs reset + init SQL scripts)
scripts/reset-database.bat   # Windows
scripts/reset-database.sh    # Unix
```

---

## Public Endpoints (no JWT required)
`/api/v1/auth/**`, `/api/v1/files/raw/**`, `/ws/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`

---

## Exception Handling
`web/GlobalExceptionHandler` centralises all error responses using `ResponseFactory`. Throw standard Java/Spring exceptions (`IllegalArgumentException`, `AccessDeniedException`, etc.) from services — the handler maps them to the correct HTTP status.

---

## Plan-First Workflow

> **RULE**: Với mọi yêu cầu phức tạp (tính năng mới, refactor lớn, thêm API, thay đổi schema), bắt buộc phải qua **2 phase**:

### Phase 1 — Phân tích & Lập kế hoạch
- Dùng prompt **`Phân tích & Lập kế hoạch`** (`.github/prompts/analyze-and-plan.prompt.md`)
- Phân tích yêu cầu + quét codebase → sinh file plan tại `.github/plans/{feature}.plan.md`
- **Không viết code thật** ở phase này

### Phase 2 — Triển khai (GPT-4.1)
- Dùng agent **`Implement Plan`** (`.github/agents/implement-plan.agent.md`)
- Agent này tự động chạy với **model GPT-4.1**
- Đọc file plan → implement theo đúng thứ tự → compile → báo cáo kết quả

### Khi nào bỏ qua Plan-First?
Chỉ được bỏ qua với các task nhỏ, đơn giản:
- Sửa lỗi typo / bug cô lập (< 10 dòng)
- Thêm Javadoc / comment
- Refactor nhỏ trong 1 file duy nhất

---

## Mandatory Skills Compliance

> **IMPORTANT**: When implementing ANY coding requirement in this project, you MUST proactively apply all relevant skills below. Do NOT wait to be asked — check each skill against the task and apply automatically.

| Task type | Required skills |
|---|---|
| Any `.java` file | `java-docs`, `java-springboot` (always) |
| New/modified service, controller, entity | `java-refactoring-extract-method`, `java-springboot` |
| Unit / integration tests | `java-junit` |
| SQL migration files (`*.sql`) | `sql-sp-generation` (instructions), `sql-code-review`, `sql-optimization` |
| Code review requested | `review-and-refactor`, `sql-code-review` (if SQL involved) |

### How to apply
1. Before generating code, read the relevant SKILL.md file(s) via the skill instruction path.
2. Apply **all** checklist items from those skills to the output.
3. If a skill cannot be fully satisfied (e.g., missing context), note it explicitly.

### Skills reference
- `java-docs` → `.github/skills/java-docs/SKILL.md`
- `java-junit` → `.github/skills/java-junit/SKILL.md`
- `java-refactoring-extract-method` → `.github/skills/java-refactoring-extract-method/SKILL.md`
- `java-springboot` → `.github/skills/java-springboot/SKILL.md`
- `review-and-refactor` → `.github/skills/review-and-refactor/SKILL.md`
- `sql-code-review` → `.github/skills/sql-code-review/SKILL.md`
- `sql-optimization` → `.github/skills/sql-optimization/SKILL.md`
