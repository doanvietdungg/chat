package chat.jace.dto.message;

import lombok.Data;

import java.util.UUID;

@Data
public class PinMessageRequest {
    private UUID messageId;
    private Integer displayOrder; // Optional: for ordering multiple pinned messages
}
