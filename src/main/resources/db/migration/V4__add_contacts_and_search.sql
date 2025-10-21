-- V4: Add contacts, search history, and user presence tables

-- Contacts table
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT unique_user_contact UNIQUE (user_id, contact_user_id),
    CONSTRAINT no_self_contact CHECK (user_id != contact_user_id)
);

-- Search history table
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    searched_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    searched_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- User presence table
CREATE TABLE user_presence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for better performance
CREATE INDEX idx_contacts_user_id ON contacts(user_id);
CREATE INDEX idx_contacts_contact_user_id ON contacts(contact_user_id);
CREATE INDEX idx_contacts_updated_at ON contacts(updated_at DESC);

CREATE INDEX idx_search_history_user_searched ON search_history(user_id, searched_at DESC);
CREATE INDEX idx_search_history_searched_user ON search_history(searched_user_id);

CREATE INDEX idx_user_presence_user_id ON user_presence(user_id);
CREATE INDEX idx_user_presence_status ON user_presence(status);
CREATE INDEX idx_user_presence_last_seen ON user_presence(last_seen_at DESC);

-- Add indexes for user search
CREATE INDEX idx_users_username_lower ON users(LOWER(username));
CREATE INDEX idx_users_email_lower ON users(LOWER(email));

-- Add constraint for presence status
ALTER TABLE user_presence ADD CONSTRAINT check_presence_status 
    CHECK (status IN ('ONLINE', 'OFFLINE', 'AWAY', 'BUSY'));