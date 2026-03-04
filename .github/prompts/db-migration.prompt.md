---
agent: 'agent'
tools: ['codebase']
description: 'Tạo Flyway DB migration script mới cho dự án Jace (PostgreSQL)'
---

# Tạo Flyway DB Migration

Tạo migration script SQL mới cho dự án Jace (PostgreSQL + Flyway).

## Yêu cầu

Hãy cung cấp:
- **Mục đích**: Ví dụ thêm bảng mới, thêm cột, tạo index...
- **Version tiếp theo**: Kiểm tra file migration mới nhất trong `src/main/resources/db/migration/` để xác định V tiếp theo

## Rules bắt buộc

### Tên file
```
V{next}__description_with_underscores.sql
```
Ví dụ: `V10__add_reactions_table.sql`

- **KHÔNG bao giờ** sửa file migration đã tồn tại (V1–V9)
- Chỉ tạo file mới

### Chuẩn SQL cho PostgreSQL
```sql
-- Tạo bảng mới
CREATE TABLE reactions (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    message_id  UUID        NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    emoji       VARCHAR(10) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_reactions_message_user_emoji UNIQUE (message_id, user_id, emoji)
);

-- Index hỗ trợ query phổ biến
CREATE INDEX idx_reactions_message_id ON reactions(message_id);
CREATE INDEX idx_reactions_user_id    ON reactions(user_id);
```

### Checklist
- [ ] Tên file đúng format `V{next}__*.sql`
- [ ] PK dùng `UUID DEFAULT gen_random_uuid()`
- [ ] Timestamps dùng `TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- [ ] FK có `ON DELETE CASCADE` hoặc `ON DELETE SET NULL` phù hợp
- [ ] Có index cho các cột thường dùng trong WHERE / JOIN
- [ ] Có constraint naming rõ ràng (`uq_`, `fk_`, `idx_`, `chk_`)
- [ ] KHÔNG có `DROP TABLE` / `ALTER TABLE ... DROP COLUMN` trên bảng production nếu chưa có dữ liệu tương ứng

### Thêm cột vào bảng đã có
```sql
-- Thêm cột nullable trước (để không break existing rows)
ALTER TABLE messages ADD COLUMN edited_at TIMESTAMPTZ;
ALTER TABLE messages ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Nếu cần index
CREATE INDEX idx_messages_is_deleted ON messages(is_deleted) WHERE is_deleted = TRUE;
```

## Pattern kiểm tra version hiện tại
Xem file mới nhất trong `src/main/resources/db/migration/` — hiện tại là `V9__add_reply_to_message.sql`, nên file tiếp theo là **V10**.
