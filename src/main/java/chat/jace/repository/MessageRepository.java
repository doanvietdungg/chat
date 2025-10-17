package chat.jace.repository;

import chat.jace.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByChatIdOrderByCreatedAtAsc(UUID chatId, Pageable pageable);
}
