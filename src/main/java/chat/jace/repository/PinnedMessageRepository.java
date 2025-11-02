package chat.jace.repository;

import chat.jace.domain.PinnedMessage;
import chat.jace.domain.PinnedMessage.PinnedMessageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, PinnedMessageId> {
    
    // Get all pinned messages in a chat, ordered by display order or pinned time
    @Query("SELECT pm FROM PinnedMessage pm WHERE pm.id.chatId = :chatId ORDER BY pm.displayOrder ASC, pm.pinnedAt DESC")
    List<PinnedMessage> findByChatIdOrderByDisplayOrder(@Param("chatId") UUID chatId);
    
    // Check if a message is pinned
    boolean existsByIdChatIdAndIdMessageId(UUID chatId, UUID messageId);
    
    // Count pinned messages in a chat
    long countByIdChatId(UUID chatId);
    
    // Delete a specific pinned message
    void deleteByIdChatIdAndIdMessageId(UUID chatId, UUID messageId);
}
