package chat.jace.dto.message;

import chat.jace.domain.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private UUID chatId;
    private UUID authorId;
    private String text;
    private MessageType type;
    private UUID fileId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
