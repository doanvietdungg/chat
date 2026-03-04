---
agent: 'agent'
tools: ['codebase']
description: 'Review code theo convention của dự án Jace — phát hiện lỗi, code smell, vi phạm patterns'
---

# Code Review — Jace Project

Review đoạn code được chọn (hoặc toàn bộ file) theo convention và best practices của dự án Jace.

## Cách dùng

1. **Chọn đoạn code** cần review (hoặc đang mở file)
2. **Chạy prompt này** — Copilot sẽ phân tích và đưa ra nhận xét

## Checklist Review

### 🚨 Critical — Vi phạm convention Jace
- [ ] Controller có trả về `ResponseEntity` trực tiếp không? → Phải dùng `ResponseFactory`
- [ ] Message trong `ResponseFactory` có phải tiếng Việt không?
- [ ] Có dùng `SecurityUtils.currentUserIdOrThrow()` để lấy user không? (không hardcode userId)
- [ ] Entity có dùng `@UuidGenerator` cho PK không?
- [ ] Flyway migration: có sửa file migration cũ không? (forbidden)

### ⚠️ Warning — Spring Boot patterns
- [ ] Service method có `@Transactional` không?
- [ ] Read-only query có dùng `@Transactional(readOnly = true)` không?
- [ ] JPA relation có dùng `FetchType.LAZY` không? (tránh N+1)
- [ ] Entity được expose trực tiếp ra API không? → Phải dùng DTO
- [ ] Có inject `ApplicationContext` hay dùng `@Autowired field injection` không? → Dùng constructor injection

### 💡 Best Practices — Java 21
- [ ] DTO có thể dùng Java **Record** không?
- [ ] `instanceof` check có thể dùng pattern matching không?
- [ ] Có `null` return không? → Dùng `Optional<T>`
- [ ] Có magic number/string nào chưa extract thành constant không?
- [ ] Method quá dài (>20 lines)? → Extract helper methods
- [ ] Có dùng `var` để tăng readability không?

### 🔒 Security
- [ ] Có kiểm tra ownership trước khi update/delete không? (user chỉ được sửa resource của mình)
- [ ] Có log thông tin nhạy cảm (password, token) không?
- [ ] Input có được validate bằng `@Valid` + Bean Validation không?

## Output format mong đợi

```
## 🚨 Critical Issues
1. [File:Line] Mô tả vấn đề → Cách sửa

## ⚠️ Warnings
1. [File:Line] Mô tả vấn đề → Cách sửa

## 💡 Suggestions
1. [File:Line] Mô tả đề xuất cải thiện

## ✅ Looks Good
- Những điểm code làm tốt
```
