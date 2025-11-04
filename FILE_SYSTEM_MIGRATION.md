# File System Migration Guide

Hướng dẫn migrate database để hỗ trợ file system mới với polymorphic associations.

## Tổng Quan

File system mới cho phép file thuộc về nhiều loại đối tượng khác nhau:
- **MESSAGE**: File trong chat message
- **POST**: File trong post (future)
- **PROFILE**: Avatar, cover của user
- **CHAT**: Avatar của group chat
- **COMMENT**: File trong comment (future)
- **NONE**: File chưa được gắn với đối tượng nào

## Thay Đổi Database Schema

### Thêm 2 Columns Mới vào Table `files`

```sql
ALTER TABLE files ADD COLUMN owner_type VARCHAR(30);
ALTER TABLE files ADD COLUMN owner_id UUID;
```

### Indexes Mới

```sql
CREATE INDEX idx_files_owner ON files(owner_type, owner_id);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_files_created_at ON files(created_at DESC);
```

---

## Cách 1: Sử dụng Flyway (Recommended)

Nếu project đang dùng Flyway, migration sẽ tự động chạy khi start application.

### File Migration

```
src/main/resources/db/migration/V8__add_file_polymorphic_fields.sql
```

### Chạy Migration

```bash
# Start application, Flyway sẽ tự động chạy migration
mvn spring-boot:run

# Hoặc build và run
mvn clean package
java -jar target/jace-chat.jar
```

### Kiểm Tra Migration Status

```bash
# Xem Flyway migration history
mvn flyway:info

# Output:
# +-----------+---------+---------------------+----------+
# | Version   | State   | Description         | Installed|
# +-----------+---------+---------------------+----------+
# | 1         | Success | init                | ...      |
# | 2         | Success | message reads       | ...      |
# | ...       | ...     | ...                 | ...      |
# | 8         | Success | add file polymorphic| ...      |
# +-----------+---------+---------------------+----------+
```

---

## Cách 2: Chạy Script Thủ Công

Nếu cần migrate database thủ công hoặc không dùng Flyway.

### Windows

```bash
cd scripts
init-file-system.bat
```

Script sẽ hỏi:
- Database password
- Sau đó tự động chạy migration

### Linux/Mac

```bash
cd scripts
chmod +x init-file-system.sh
./init-file-system.sh
```

### Hoặc Chạy SQL Trực Tiếp

```bash
psql -h localhost -U postgres -d jace_chat -f scripts/init-file-system.sql
```

---

## Migration Script Chi Tiết

### Bước 1: Thêm Columns

```sql
ALTER TABLE files ADD COLUMN IF NOT EXISTS owner_type VARCHAR(30);
ALTER TABLE files ADD COLUMN IF NOT EXISTS owner_id UUID;
```

### Bước 2: Set Default Values

```sql
-- Set NONE cho files chưa có owner
UPDATE files 
SET owner_type = 'NONE' 
WHERE owner_type IS NULL;
```

### Bước 3: Migrate Existing Data

```sql
-- Gắn files với messages hiện có
UPDATE files f
SET owner_type = 'MESSAGE',
    owner_id = m.id
FROM messages m
WHERE m.file_id = f.id
  AND f.owner_type = 'NONE';
```

### Bước 4: Tạo Indexes

```sql
CREATE INDEX IF NOT EXISTS idx_files_owner 
ON files(owner_type, owner_id);

CREATE INDEX IF NOT EXISTS idx_files_uploaded_by 
ON files(uploaded_by);

CREATE INDEX IF NOT EXISTS idx_files_created_at 
ON files(created_at DESC);
```

### Bước 5: Add Constraints

```sql
ALTER TABLE files 
ADD CONSTRAINT files_owner_type_check 
CHECK (owner_type IN ('MESSAGE', 'POST', 'PROFILE', 'CHAT', 'COMMENT', 'NONE'));
```

---

## Verification Queries

### Kiểm Tra Migration Thành Công

```sql
-- Xem cấu trúc table files
\d files

-- Output should show:
-- owner_type | character varying(30)
-- owner_id   | uuid
```

### Xem Thống Kê Files

```sql
SELECT 
    owner_type,
    COUNT(*) AS file_count,
    SUM(size) AS total_size_bytes,
    ROUND(SUM(size)::numeric / 1024 / 1024, 2) AS total_size_mb
FROM files
GROUP BY owner_type
ORDER BY file_count DESC;
```

**Expected Output:**
```
 owner_type | file_count | total_size_bytes | total_size_mb
------------+------------+------------------+---------------
 MESSAGE    |         45 |        12458900  |        11.88
 NONE       |          3 |          245678  |         0.23
```

### Kiểm Tra Files Trong Messages

```sql
SELECT 
    f.id AS file_id,
    f.name AS file_name,
    f.owner_type,
    m.id AS message_id,
    m.text AS message_text,
    m.type AS message_type
FROM files f
JOIN messages m ON f.owner_id = m.id
WHERE f.owner_type = 'MESSAGE'
ORDER BY f.created_at DESC
LIMIT 10;
```

### Tìm Orphaned Files

```sql
-- Files có owner_type = MESSAGE nhưng message không tồn tại
SELECT 
    f.id,
    f.name,
    f.owner_id AS missing_message_id,
    f.created_at
FROM files f
WHERE f.owner_type = 'MESSAGE'
  AND NOT EXISTS (
      SELECT 1 FROM messages m WHERE m.id = f.owner_id
  );
```

---

## Rollback (Nếu Cần)

### Xóa Columns Mới

```sql
ALTER TABLE files DROP COLUMN IF EXISTS owner_type;
ALTER TABLE files DROP COLUMN IF EXISTS owner_id;
```

### Xóa Indexes

```sql
DROP INDEX IF EXISTS idx_files_owner;
DROP INDEX IF EXISTS idx_files_uploaded_by;
DROP INDEX IF EXISTS idx_files_created_at;
```

---

## Testing After Migration

### 1. Test Upload File

```bash
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.jpg"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "...",
    "ownerType": "NONE",
    "ownerId": null
  }
}
```

### 2. Test Send Message với File

```bash
curl -X POST http://localhost:8080/api/v1/chats/{chatId}/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Test file",
    "type": "IMAGE",
    "fileId": "file-id-from-upload"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "msg-id",
    "fileId": "file-id",
    "file": {
      "id": "file-id",
      "url": "/api/v1/files/raw/...",
      "ownerType": "MESSAGE",
      "ownerId": "msg-id"
    }
  }
}
```

### 3. Verify Database

```sql
-- Check file owner updated
SELECT id, name, owner_type, owner_id 
FROM files 
WHERE id = 'file-id-from-upload';

-- Should show:
-- owner_type = 'MESSAGE'
-- owner_id = 'msg-id'
```

---

## Troubleshooting

### Error: Column already exists

```
ERROR: column "owner_type" of relation "files" already exists
```

**Solution:** Migration đã chạy rồi, không cần chạy lại.

### Error: Permission denied

```
ERROR: permission denied for table files
```

**Solution:** Đảm bảo database user có quyền ALTER TABLE.

```sql
GRANT ALL PRIVILEGES ON TABLE files TO your_user;
```

### Files không được gắn owner

**Check:**
```sql
SELECT COUNT(*) FROM files WHERE owner_type = 'NONE';
```

**Fix:**
```sql
-- Re-run migration for existing message files
UPDATE files f
SET owner_type = 'MESSAGE',
    owner_id = m.id
FROM messages m
WHERE m.file_id = f.id
  AND f.owner_type = 'NONE';
```

---

## Performance Notes

### Index Usage

```sql
-- Query files by owner
EXPLAIN ANALYZE
SELECT * FROM files 
WHERE owner_type = 'MESSAGE' AND owner_id = 'some-uuid';

-- Should use: idx_files_owner
```

### Query Optimization

```sql
-- Get all files of a user
SELECT * FROM files 
WHERE uploaded_by = 'user-uuid'
ORDER BY created_at DESC;

-- Uses: idx_files_uploaded_by, idx_files_created_at
```

---

## Summary

✅ **Migration Files:**
- `V8__add_file_polymorphic_fields.sql` - Flyway migration
- `init-file-system.sql` - Manual migration script
- `init-file-system.bat` - Windows script
- `init-file-system.sh` - Linux/Mac script

✅ **Changes:**
- Added `owner_type` column (VARCHAR(30))
- Added `owner_id` column (UUID)
- Created 3 indexes for performance
- Migrated existing file-message relationships

✅ **Backward Compatible:**
- `file_id` column in messages table vẫn giữ nguyên
- Existing queries vẫn hoạt động
- API response bao gồm cả `fileId` và `file` object

✅ **Next Steps:**
1. Run migration (Flyway hoặc manual)
2. Verify với queries trên
3. Test upload và send message
4. Update frontend để sử dụng `message.file.url`
