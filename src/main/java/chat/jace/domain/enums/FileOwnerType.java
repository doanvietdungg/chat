package chat.jace.domain.enums;

/**
 * Defines the type of entity that owns a file.
 * This enables polymorphic file associations.
 */
public enum FileOwnerType {
    MESSAGE,    // File belongs to a chat message
    POST,       // File belongs to a post (future feature)
    PROFILE,    // File belongs to user profile (avatar, cover)
    CHAT,       // File belongs to chat (group avatar)
    COMMENT,    // File belongs to a comment (future feature)
    NONE        // File not yet associated with any entity
}
