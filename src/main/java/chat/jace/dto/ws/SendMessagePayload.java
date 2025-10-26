package chat.jace.dto.ws;

import chat.jace.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessagePayload {
    // Optional for the first message before a chat is created
    private UUID chatId;

    // Required when chatId is null (first message use case)
    private UUID recipientId;

    @NotBlank
    private String text;

    private MessageType type = MessageType.TEXT;
}
