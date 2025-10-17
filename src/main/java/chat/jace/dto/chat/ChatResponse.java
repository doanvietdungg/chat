package chat.jace.dto.chat;

import chat.jace.domain.enums.ChatType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatResponse {
    private UUID id;
    private ChatType type;
    private String title;
    private String description;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
