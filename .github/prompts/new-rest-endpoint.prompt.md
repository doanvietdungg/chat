---
agent: 'agent'
tools: ['codebase']
description: 'Tạo REST endpoint mới theo đúng convention của dự án Jace'
---

# Tạo REST Endpoint Mới

Tạo một REST endpoint mới theo đúng convention của dự án Jace.

## Yêu cầu

Hãy cung cấp thông tin sau:
- **Resource**: Tên resource (ví dụ: `Message`, `Chat`, `Contact`)
- **Action**: Hành động (ví dụ: getAll, getById, create, update, delete)
- **HTTP Method + Path**: Ví dụ `GET /api/v1/messages`

## Rules bắt buộc

### Controller
- Đặt trong `controller/` với annotation `@RestController @RequestMapping("/api/v1/{resource}")`
- **KHÔNG** trả về `ResponseEntity` trực tiếp — luôn dùng `ResponseFactory`:
  ```java
  return ResponseFactory.success(dto, "Lấy thành công");
  return ResponseFactory.created(dto, "Tạo thành công");
  return ResponseFactory.noContent();
  ```
- Messages phải bằng **tiếng Việt**
- Lấy user hiện tại bằng: `UUID me = SecurityUtils.currentUserIdOrThrow();`
- Validate request body bằng `@Valid`

### Service
- Đặt trong `service/`, tách interface và implementation
- Annotation `@Service` + `@Transactional` ở method level
- Throw `IllegalArgumentException` hoặc `AccessDeniedException` — **không** tự xử lý HTTP status

### DTO
- Dùng Java **Record** cho request/response DTO:
  ```java
  public record CreateMessageRequest(@NotBlank String content, UUID chatId) {}
  public record MessageResponse(UUID id, String content, Instant createdAt) {}
  ```

### Checklist
- [ ] Controller method dùng `ResponseFactory`
- [ ] Service method có `@Transactional`
- [ ] DTO dùng Record
- [ ] Message tiếng Việt
- [ ] `SecurityUtils.currentUserIdOrThrow()` nếu cần auth

## Ví dụ output mong đợi

```java
// Controller
@GetMapping("/{id}")
public ResponseEntity<?> getById(@PathVariable UUID id) {
    var dto = messageService.getById(id);
    return ResponseFactory.success(dto, "Lấy tin nhắn thành công");
}

// Service
@Transactional(readOnly = true)
public MessageResponse getById(UUID id) {
    return messageRepository.findById(id)
        .map(messageMapper::toResponse)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin nhắn"));
}
```
