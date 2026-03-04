---
agent: 'agent'
tools: ['codebase']
description: 'Tạo JPA Entity mới theo đúng convention của dự án Jace'
---

# Tạo JPA Entity Mới

Tạo một JPA Entity mới theo đúng convention của dự án Jace.

## Yêu cầu

Hãy cung cấp:
- **Tên entity**: Ví dụ `Notification`, `Reaction`
- **Các fields**: Danh sách fields và kiểu dữ liệu
- **Quan hệ**: ManyToOne, OneToMany, ManyToMany với entity nào

## Rules bắt buộc

### Entity class
```java
@Entity
@Table(name = "entity_name")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyEntity {

    @Id
    @UuidGenerator
    private UUID id;

    // fields...

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### Quan hệ
- `@ManyToOne(fetch = FetchType.LAZY)` — luôn dùng LAZY để tránh N+1
- `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL, orphanRemoval = true)`
- Tránh bidirectional nếu không thực sự cần

### Repository
- Tạo `repository/{Entity}Repository.java` extends `JpaRepository<Entity, UUID>`
- Thêm custom query bằng Spring Data method naming hoặc `@Query` với JPQL

### Flyway Migration
- Tạo file `V{next}__add_{entity_name}.sql` trong `db/migration/`
- **KHÔNG** sửa file migration cũ

### Checklist
- [ ] Dùng `@UuidGenerator` cho PK
- [ ] Có Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- [ ] Relations dùng `FetchType.LAZY`
- [ ] Có `createdAt` / `updatedAt` với `@PrePersist` / `@PreUpdate`
- [ ] Tạo Repository tương ứng
- [ ] Tạo Flyway migration file

## Ví dụ

```java
@Entity
@Table(name = "reactions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Reaction {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}
```
