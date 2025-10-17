create table if not exists message_reads (
    message_id uuid not null references messages(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    read_at timestamp with time zone not null default now(),
    primary key (message_id, user_id)
);
