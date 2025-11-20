package chat.jace.dto.message;

import chat.jace.domain.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageCreateRequest {
    @NotNull
    private UUID chatId;

    private String text;  // Optional when fileId is provided

    @NotNull
    private MessageType type = MessageType.TEXT;

    private UUID fileId;  // Optional: ID of uploaded file

    private UUID replyToId; // Optional: message ID to reply to

    private UUID forwardedFromId; // Optional: message ID that is being forwarded
}
