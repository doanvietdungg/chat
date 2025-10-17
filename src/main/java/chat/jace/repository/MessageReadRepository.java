package chat.jace.repository;

import chat.jace.domain.MessageRead;
import chat.jace.domain.MessageRead.MessageReadId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
    List<MessageRead> findByMessageId(UUID messageId);
    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
}
