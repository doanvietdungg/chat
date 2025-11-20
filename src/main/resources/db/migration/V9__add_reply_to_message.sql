-- Add reply_to_id column to support message replies
alter table if exists messages
    add column if not exists reply_to_id uuid references messages(id) on delete set null;

-- Optional index for faster lookups by reply_to_id
create index if not exists idx_messages_reply_to_id on messages(reply_to_id);

