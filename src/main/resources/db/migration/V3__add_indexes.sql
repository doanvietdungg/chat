-- Users
create index if not exists idx_users_email on users (email);
create index if not exists idx_users_username on users (username);

-- Chats
create index if not exists idx_chats_created_by on chats (created_by);
create index if not exists idx_chats_created_at on chats (created_at);

-- Participants
create index if not exists idx_participants_chat on participants (chat_id);
create index if not exists idx_participants_user on participants (user_id);

-- Messages
create index if not exists idx_messages_chat_created on messages (chat_id, created_at);
create index if not exists idx_messages_author on messages (author_id);

-- Message reads
create index if not exists idx_message_reads_message on message_reads (message_id);
create index if not exists idx_message_reads_user on message_reads (user_id);

-- Files
create index if not exists idx_files_uploaded_by on files (uploaded_by);
create index if not exists idx_files_created_at on files (created_at);

-- Notifications
create index if not exists idx_notifications_user on notifications (user_id);
create index if not exists idx_notifications_created_at on notifications (created_at);
