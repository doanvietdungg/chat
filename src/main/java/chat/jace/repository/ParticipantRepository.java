package chat.jace.repository;

import chat.jace.domain.Participant;
import chat.jace.domain.Participant.ParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
    boolean existsByChatIdAndUserId(UUID chatId, UUID userId);
    List<Participant> findByChatId(UUID chatId);
    List<Participant> findByUserId(UUID userId);
    Optional<Participant> findByChatIdAndUserId(UUID chatId, UUID userId);
}
