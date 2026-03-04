---
agent: 'agent'
tools: ['codebase', 'edit/editFiles', 'search/codebase', 'problems']
description: 'Phân tích yêu cầu, quét codebase, rồi sinh ra file plan chi tiết trong .github/plans/ — dùng trước khi implement'
---

# Phân tích & Lập kế hoạch triển khai

Phân tích toàn diện yêu cầu, sau đó tạo một file plan chi tiết tại `.github/plans/{tên-tính-năng}.plan.md`.  
**Không viết code thật** ở bước này — chỉ lập kế hoạch.

---

## Bước 1 — Thu thập thông tin

Trước khi bắt đầu, xác định:

1. **Yêu cầu là gì?** (feature mới, refactor, fix bug, thêm API, ...)
2. **Scope**: những domain nào bị ảnh hưởng? (Chat, Message, User, Participant, ...)
3. **Migration cần không?** (thêm bảng/cột/index → cần file Flyway mới)

---

## Bước 2 — Quét codebase

Đọc và phân tích các file liên quan:

- `src/main/java/chat/jace/domain/` — kiểm tra entity đã có chưa
- `src/main/java/chat/jace/repository/` — repository hiện tại
- `src/main/java/chat/jace/service/` — service hiện tại
- `src/main/java/chat/jace/controller/` — controller hiện tại
- `src/main/java/chat/jace/dto/` — DTO hiện tại
- `src/main/resources/db/migration/` — xác định version Flyway tiếp theo

---

## Bước 3 — Sinh file plan

Tạo file tại `.github/plans/{tên-kebab-case}.plan.md` theo template sau:

```markdown
---
feature: <tên tính năng>
created: <ngày tạo YYYY-MM-DD>
status: pending
implement-with: implement-plan agent (GPT-4.1)
---

# Plan: <Tên tính năng>

## Tổng quan
<Mô tả ngắn mục đích>

## Phân tích tác động
| Layer | File/Class | Thay đổi |
|---|---|---|
| Migration | `V{n}__xxx.sql` | Tạo mới / Không cần |
| Entity | `domain/XxxEntity.java` | Tạo mới / Sửa |
| Repository | `repository/XxxRepository.java` | Tạo mới / Sửa |
| DTO | `dto/xxx/XxxRequest.java` | Tạo mới / Sửa |
| Service | `service/XxxService.java` | Tạo mới / Sửa method |
| Controller | `controller/XxxController.java` | Tạo mới / Thêm endpoint |

## Chi tiết từng bước

### [1] Migration SQL (nếu cần)
- File: `V{n}__description.sql`
- Nội dung: <liệt kê bảng/cột/index cần tạo, kiểu dữ liệu, constraint>

### [2] Entity (nếu cần)
- Class: `domain/XxxEntity.java`
- Fields: <tên, kiểu, annotation JPA>
- Relations: <@ManyToOne, @OneToMany, ...>

### [3] Repository (nếu cần)
- Interface: `repository/XxxRepository.java`
- Custom queries cần thêm: <tên method + JPQL/native query>

### [4] DTO
- Request record: `dto/xxx/XxxRequest.java` — fields, validation
- Response record: `dto/xxx/XxxResponse.java` — fields

### [5] Service
- Class: `service/XxxService.java`
- Methods cần implement:
  - `methodName(params)` — mô tả logic, điều kiện throw exception, transaction

### [6] Controller
- Class: `controller/XxxController.java`
- Endpoints:
  - `METHOD /api/v1/path` → gọi service method → `ResponseFactory.xxx(..., "message tiếng Việt")`

## Edge cases & Validation
- <Liệt kê các trường hợp đặc biệt cần xử lý>

## WebSocket Events (nếu cần)
- <Liệt kê events cần broadcast, destination, payload>

## Checklist
- [ ] Migration viết đúng chuẩn PostgreSQL
- [ ] Entity dùng @UuidGenerator cho PK
- [ ] Controller dùng ResponseFactory + message tiếng Việt
- [ ] Service throw exception đúng loại
- [ ] Auth dùng SecurityUtils.currentUserIdOrThrow()
```

---

## Bước 4 — Xác nhận

Sau khi tạo file plan, hiển thị đường dẫn file và hỏi:
> "Plan đã được lưu tại `.github/plans/{tên}.plan.md`. Bạn có muốn điều chỉnh gì trước khi chuyển sang implement không?"

Khi người dùng xác nhận OK → hướng dẫn:
> "Mở **Copilot Chat**, chọn agent **Implement Plan** (GPT-4.1), rồi gõ: `implement .github/plans/{tên}.plan.md`"
