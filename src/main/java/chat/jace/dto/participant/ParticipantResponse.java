package chat.jace.dto.participant;

import chat.jace.domain.enums.ParticipantRole;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ParticipantResponse {
    private UUID chatId;
    private UUID userId;
    private ParticipantRole role;
    private OffsetDateTime joinedAt;
}
