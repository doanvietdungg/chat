-- Create pinned_messages table
CREATE TABLE pinned_messages (
    chat_id UUID NOT NULL,
    message_id UUID NOT NULL,
    pinned_by UUID NOT NULL,
    pinned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    display_order INTEGER,
    
    PRIMARY KEY (chat_id, message_id),
    
    CONSTRAINT fk_pinned_messages_chat 
        FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    CONSTRAINT fk_pinned_messages_message 
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_pinned_messages_user 
        FOREIGN KEY (pinned_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_pinned_messages_chat_id ON pinned_messages(chat_id);
CREATE INDEX idx_pinned_messages_pinned_at ON pinned_messages(pinned_at);
CREATE INDEX idx_pinned_messages_display_order ON pinned_messages(display_order);

-- Add comment
COMMENT ON TABLE pinned_messages IS 'Stores pinned messages in chats';
COMMENT ON COLUMN pinned_messages.display_order IS 'Optional order for displaying multiple pinned messages';
