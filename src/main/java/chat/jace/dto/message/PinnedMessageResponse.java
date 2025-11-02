package chat.jace.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PinnedMessageResponse {
    private UUID chatId;
    private UUID messageId;
    private UUID pinnedBy;
    private OffsetDateTime pinnedAt;
    private Integer displayOrder;
    
    // Include the actual message data for convenience
    private MessageResponse message;
}
