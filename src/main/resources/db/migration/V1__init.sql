-- Flyway baseline schema for core chat models
create extension if not exists "uuid-ossp";

create table if not exists users (
    id uuid primary key,
    username varchar(50) unique not null,
    email varchar(255) unique not null,
    password_hash varchar(255) not null,
    avatar_url varchar(512),
    email_verified boolean not null default false,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table if not exists chats (
    id uuid primary key,
    type varchar(20) not null,
    title varchar(255),
    description text,
    settings jsonb,
    created_by uuid not null references users(id),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table if not exists participants (
    chat_id uuid not null references chats(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    role varchar(20) not null,
    joined_at timestamp with time zone not null default now(),
    primary key (chat_id, user_id)
);

create table if not exists files (
    id uuid primary key,
    name varchar(255) not null,
    size bigint not null,
    content_type varchar(100),
    url varchar(1024) not null,
    uploaded_by uuid references users(id),
    created_at timestamp with time zone not null default now()
);

create table if not exists messages (
    id uuid primary key,
    chat_id uuid not null references chats(id) on delete cascade,
    author_id uuid not null references users(id) on delete set null,
    text text,
    type varchar(30) not null,
    file_id uuid references files(id) on delete set null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table if not exists reactions (
    message_id uuid not null references messages(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    emoji varchar(64) not null,
    primary key (message_id, user_id, emoji)
);

create table if not exists notifications (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    type varchar(50) not null,
    title varchar(255),
    body text,
    data jsonb,
    read boolean not null default false,
    created_at timestamp with time zone not null default now()
);
